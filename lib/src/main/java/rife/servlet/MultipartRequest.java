/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.servlet;

import java.io.*;

import rife.config.RifeConfig;
import rife.engine.UploadedFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import rife.engine.exceptions.*;

class MultipartRequest {
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";
    private static final String BOUNDARY_PREFIX = "boundary=";
    private static final int BOUNDARY_PREFIX_LENGTH = BOUNDARY_PREFIX.length();
    private static final String CONTENT_DISPOSITION_PREFIX = "content-disposition: ";
    private static final int CONTENT_DISPOSITION_PREFIX_LENGTH = CONTENT_DISPOSITION_PREFIX.length();
    private static final String FIELD_NAME_PREFIX = "name=\"";
    private static final int FIELD_NAME_PREFIX_LENGTH = FIELD_NAME_PREFIX.length();
    private static final String FILENAME_PREFIX = "filename=\"";
    private static final int FILENAME_PREFIX_LENGTH = FILENAME_PREFIX.length();
    private static final String QUOTE = "\"";
    private static final String FORM_DATA_DISPOSITION = "form-data";
    private static final String DEFAULT_ENCODING = "UTF-8";

    private File uploadDirectory_ = null;

    private final HttpServletRequest request_;
    private final byte[] parameterBuffer_;
    private final byte[] fileBuffer_;
    private String boundary_ = null;
    private ServletInputStream input_ = null;
    private String rncoding_ = DEFAULT_ENCODING;

    private final HashMap<String, String[]> parameters_;
    private final HashMap<String, UploadedFile[]> files_;

    MultipartRequest(HttpServletRequest request) throws MultipartRequestException {
        if (null == request) throw new IllegalArgumentException("request can't be null");

        request_ = request;
        parameters_ = new HashMap<String, String[]>();
        files_ = new HashMap<String, UploadedFile[]>();
        parameterBuffer_ = new byte[8 * 1024];
        fileBuffer_ = new byte[100 * 1024];

        checkUploadDirectory();
        initialize();
        checkInputStart();
        readParts();
    }

    static boolean isValidContentType(String type) {
        return null != type &&
            type.toLowerCase().startsWith(MULTIPART_CONTENT_TYPE);
    }

    Map<String, String[]> getParameterMap() {
        return parameters_;
    }

    Map<String, UploadedFile[]> getFileMap() {
        return files_;
    }

    void setEncoding(String encoding) {
        assert encoding != null;

        rncoding_ = encoding;
    }

    private void checkUploadDirectory() throws MultipartRequestException {
        uploadDirectory_ = new File(RifeConfig.engine().fileUploadPath());
        uploadDirectory_.mkdirs();

        if (!uploadDirectory_.exists() ||
            !uploadDirectory_.isDirectory() ||
            !uploadDirectory_.canWrite()) {
            throw new MultipartInvalidUploadDirectoryException(uploadDirectory_);
        }
    }

    private void initialize() throws MultipartRequestException {
        // Check the content type to is correct to support a multipart request
        // Access header two ways to work around WebSphere oddities
        String type = null;
        var type_header = request_.getHeader(CONTENT_TYPE_HEADER);
        var type_method = request_.getContentType();

        // If one value is null, choose the other value
        if (type_header == null &&
            type_method != null) {
            type = type_method;
        } else if (type_method == null &&
            type_header != null) {
            type = type_header;
        }
        // If neither value is null, choose the longer value
        else if (type_header != null &&
            type_method != null) {
            type = (type_header.length() > type_method.length() ? type_header : type_method);
        }

        // ensure that the content-type is correct
        if (!isValidContentType(type)) {
            throw new MultipartInvalidContentTypeException(type);
        }

        // extract the boundary seperator that is used by this request
        boundary_ = extractBoundary(type);
        if (null == boundary_) {
            throw new MultipartMissingBoundaryException();
        }

        // obtain the input stream
        try {
            input_ = request_.getInputStream();
        } catch (IOException e) {
            throw new MultipartInputErrorException(e);
        }
    }

    private void checkInputStart() throws MultipartRequestException {
        // Read the first line, should be the first boundary
        var line = readLine();
        if (null == line) {
            throw new MultipartUnexpectedEndingException();
        }

        // Verify that the line is the boundary
        if (!line.startsWith(boundary_)) {
            throw new MultipartInvalidBoundaryException(boundary_, line);
        }
    }

    private void readParts() throws MultipartRequestException {
        var more_parts = true;

        while (more_parts) {
            more_parts = readNextPart();
        }
    }

    private String extractBoundary(String line) {
        // Use lastIndexOf() because IE 4.01 on Win98 has been known to send the
        // "boundary=" string multiple times.
        var index = line.lastIndexOf(BOUNDARY_PREFIX);

        if (-1 == index) {
            return null;
        }

        // start from after the boundary prefix
        var boundary = line.substring(index + BOUNDARY_PREFIX_LENGTH);
        if ('"' == boundary.charAt(0)) {
            // The boundary is enclosed in quotes, strip them
            index = boundary.lastIndexOf('"');
            boundary = boundary.substring(1, index);
        }

        // The real boundary is always preceeded by an extra "--"
        boundary = "--" + boundary;

        return boundary;
    }

    private String readLine() throws MultipartRequestException {
        var line_buffer = new StringBuilder();

        var result = 0;
        do {
            try {
                result = input_.readLine(parameterBuffer_, 0, parameterBuffer_.length);
            } catch (IOException e) {
                throw new MultipartInputErrorException(e);
            }

            if (result != -1) {
                try {
                    line_buffer.append(new String(parameterBuffer_, 0, result, rncoding_));
                } catch (UnsupportedEncodingException e) {
                    throw new MultipartInputErrorException(e);
                }
            }
        }
        // if the buffer wasn't completely filled, the end of the input has been reached
        while (result == parameterBuffer_.length);

        // if nothing was read, the end of the stream must have been reached
        if (line_buffer.length() == 0) {
            return null;
        }

        // Cut off the trailing \n or \r\n
        // It should always be \r\n but IE5 sometimes does just \n
        var line_length = line_buffer.length();
        if (line_length >= 2 &&
            '\r' == line_buffer.charAt(line_length - 2)) {
            // remove the trailing \r\n
            line_buffer.setLength(line_length - 2);
        } else if (line_length >= 1 &&
            '\n' == line_buffer.charAt(line_length - 1)) {
            // remove the trailing \n
            line_buffer.setLength(line_length - 1);
        }

        return line_buffer.toString();
    }

    private boolean readNextPart() throws MultipartRequestException {
        // Read the headers; they look like this (not all may be present):
        // Content-Disposition: form-data; name="field1"; filename="file1.txt"
        // Content-Type: type/subtype
        // Content-Transfer-Encoding: binary
        var headers = new ArrayList<String>();

        var line = readLine();
        // When no next line could be read, the end was reached.
        // IE4 on Mac sends an empty line at the end; treat that as the ending too.
        if (null == line ||
            0 == line.length()) {
            // No parts left, we're done
            return false;
        }

        // Read the following header lines we hit an empty line
        // A line starting with whitespace is considered a continuation;
        // that requires a little special logic.
        while (null != line &&
            line.length() > 0) {
            String next_line = null;
            boolean obtain_next_line = true;
            while (obtain_next_line) {
                next_line = readLine();

                if (next_line != null &&
                    (next_line.startsWith(" ") ||
                        next_line.startsWith("\t"))) {
                    line = line + next_line;
                } else {
                    obtain_next_line = false;
                }
            }
            // Add the line to the header list
            headers.add(line);
            line = next_line;
        }

        // If we got a null above, it's the end
        if (line == null) {
            return false;
        }

        String fieldname = null;
        String filename = null;
        String content_type = "text/plain";  // rfc1867 says this is the default

        String[] disposition_info = null;

        for (String headerline : headers) {
            if (headerline.toLowerCase().startsWith(CONTENT_DISPOSITION_PREFIX)) {
                // Parse the content-disposition line
                disposition_info = extractDispositionInfo(headerline);

                fieldname = disposition_info[0];
                filename = disposition_info[1];
            } else if (headerline.toLowerCase().startsWith(CONTENT_TYPE_HEADER)) {
                // Get the content type, or null if none specified
                String type = extractContentType(headerline);
                if (type != null) {
                    content_type = type;
                }
            }
        }

        if (null == filename) {
            // This is a parameter
            var new_value = readParameter();
            var values = parameters_.get(fieldname);
            String[] new_values = null;
            if (null == values) {
                new_values = new String[1];
            } else {
                new_values = new String[values.length + 1];
                System.arraycopy(values, 0, new_values, 0, values.length);
            }
            new_values[new_values.length - 1] = new_value;
            parameters_.put(fieldname, new_values);
        } else {
            // This is a file
            if (filename.equals("")) {
                // empty filename, probably an "empty" file param
                filename = null;
            }

            var new_file = new UploadedFile(filename, content_type);
            readAndSaveFile(new_file, fieldname);
            var files = files_.get(fieldname);
            UploadedFile[] new_files = null;
            if (null == files) {
                new_files = new UploadedFile[1];
            } else {
                new_files = new UploadedFile[files.length + 1];
                System.arraycopy(files, 0, new_files, 0, files.length);
            }
            new_files[new_files.length - 1] = new_file;
            files_.put(fieldname, new_files);
        }

        return true;
    }

    private String[] extractDispositionInfo(String dispositionLine) throws MultipartRequestException {
        // Return the line's data as an array: disposition, name, filename, full filename
        var result = new String[3];
        var lowcase_line = dispositionLine.toLowerCase();
        String fieldname = null;
        String filename = null;
        String filename_full = null;

        // Get the content disposition, should be "form-data"
        int start = lowcase_line.indexOf(CONTENT_DISPOSITION_PREFIX);
        int end = lowcase_line.indexOf(";");
        if (-1 == start ||
            -1 == end) {
            throw new MultipartCorruptContentDispositionException(dispositionLine);
        }
        var disposition = lowcase_line.substring(start + CONTENT_DISPOSITION_PREFIX_LENGTH, end);
        if (!disposition.equals(FORM_DATA_DISPOSITION)) {
            throw new MultipartInvalidContentDispositionException(dispositionLine);
        }

        // Get the field name, start at last semicolon
        start = lowcase_line.indexOf(FIELD_NAME_PREFIX, end);
        end = lowcase_line.indexOf(QUOTE, start + FIELD_NAME_PREFIX_LENGTH);
        if (-1 == start ||
            -1 == end) {
            throw new MultipartCorruptContentDispositionException(dispositionLine);
        }
        fieldname = dispositionLine.substring(start + FIELD_NAME_PREFIX_LENGTH, end);

        // Get the filename, if given
        start = lowcase_line.indexOf(FILENAME_PREFIX, end + 2); // after quote and space)
        end = lowcase_line.indexOf(QUOTE, start + FILENAME_PREFIX_LENGTH);
        if (start != -1 &&
            end != -1) {
            filename_full = dispositionLine.substring(start + FILENAME_PREFIX_LENGTH, end);
            filename = filename_full;

            // The filename may contain a full path.  Cut to just the filename.
            int last_slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
            if (last_slash > -1) {
                // only take the filename (after the last slash)
                filename = filename.substring(last_slash + 1);
            }
        }

        // Return a String array: name, filename, full filename
        // empty filename denotes no file posted!
        result[0] = fieldname;
        result[1] = filename;
        result[2] = filename_full;

        return result;
    }

    private String extractContentType(String contentTypeLine) throws MultipartRequestException {
        String result = null;
        var lowcase_line = contentTypeLine.toLowerCase();

        // Get the content type, if any
        if (lowcase_line.startsWith(CONTENT_TYPE_HEADER)) {
            int seperator_location = lowcase_line.indexOf(" ");
            if (-1 == seperator_location) {
                throw new MultipartCorruptContentTypeException(contentTypeLine);
            }
            result = lowcase_line.substring(seperator_location + 1);
        } else if (lowcase_line.length() != 0) {
            // no content type, so should be empty
            throw new MultipartCorruptContentTypeException(contentTypeLine);
        }

        return result;
    }

    private String readParameter() throws MultipartRequestException {
        var result = new StringBuilder();
        String line = null;
        while ((line = readLine()) != null) {
            if (line.startsWith(boundary_)) {
                break;
            }
            // add the \r\n in case there are many lines
            result.append(line).append("\r\n");
        }

        // nothing read
        if (0 == result.length()) {
            return null;
        }

        // cut off the last line's \r\n
        result.setLength(result.length() - 2);

        return result.toString();
    }

    private void readAndSaveFile(UploadedFile file, String name) throws MultipartRequestException {
        assert file != null;

        File tmp_file = null;
        FileOutputStream output_stream = null;
        BufferedOutputStream output = null;

        try {
            tmp_file = File.createTempFile("upl", ".tmp", uploadDirectory_);
        } catch (IOException e) {
            throw new MultipartFileErrorException(name, e);
        }
        try {
            output_stream = new FileOutputStream(tmp_file);
        } catch (FileNotFoundException e) {
            throw new MultipartFileErrorException(name, e);
        }
        output = new BufferedOutputStream(output_stream, 8 * 1024); // 8K

        long downloaded_size = 0;
        int result = -1;
        String line = null;
        int line_length = 0;

        // ServletInputStream.readLine() has the annoying habit of
        // adding a \r\n to the end of the last line.
        // Since we want a byte-for-byte transfer, we have to cut those chars.
        var rnflag = false;
        try {
            while ((result = input_.readLine(fileBuffer_, 0, fileBuffer_.length)) != -1) {
                // Check for boundary
                if (result > 2 &&
                    '-' == fileBuffer_[0] &&
                    '-' == fileBuffer_[1]) {
                    // quick pre-check
                    try {
                        line = new String(fileBuffer_, 0, result, rncoding_);
                    } catch (UnsupportedEncodingException e) {
                        throw new MultipartFileErrorException(name, e);
                    }

                    if (line.startsWith(boundary_)) {
                        break;
                    }
                }

                // Are we supposed to write \r\n for the last iteration?
                if (rnflag &&
                    output != null) {
                    output.write('\r');
                    output.write('\n');
                    rnflag = false;
                }

                // postpone any ending \r\n
                if (result >= 2 &&
                    '\r' == fileBuffer_[result - 2] &&
                    '\n' == fileBuffer_[result - 1]) {
                    line_length = result - 2; // skip the last 2 chars
                    rnflag = true;  // make a note to write them on the next iteration
                } else {
                    line_length = result;
                }

                // increase size count
                if (output != null &&
                    RifeConfig.engine().fileUploadSizeCheck()) {
                    downloaded_size += line_length;

                    if (downloaded_size > RifeConfig.engine().fileUploadSizeLimit()) {
                        file.setSizeExceeded(true);
                        output.close();
                        output = null;
                        tmp_file.delete();
                        tmp_file = null;
                        if (RifeConfig.engine().fileUploadSizeException()) {
                            throw new MultipartFileTooBigException(name, RifeConfig.engine().fileUploadSizeLimit());
                        }
                    }
                }

                // write the content
                if (output != null) {
                    output.write(fileBuffer_, 0, line_length);
                }
            }
        } catch (IOException e) {
            throw new MultipartFileErrorException(name, e);
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                    output_stream.close();
                }
            } catch (IOException e) {
                throw new MultipartFileErrorException(name, e);
            }
        }

        if (tmp_file != null) {
            file.setTempFile(tmp_file);
        }
    }
}
