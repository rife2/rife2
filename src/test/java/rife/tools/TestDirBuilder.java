/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rife.bld.operations.CreateBlankOperation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static rife.tools.FileUtils.path;

public class TestDirBuilder {
    @TempDir
    File tmp;

    @Test
    void testDirBuilderInstantiation()
    throws IOException {
        var builder1 = new DirBuilder(tmp);
        assertNotNull(builder1);

        var result1 = new StringBuilder();
        var builder2 = new DirBuilder(tmp, d -> result1.append("one"));
        assertNotNull(builder2);
        assertEquals(result1.toString(), "one");
    }

    @Test
    void testDirBuilderInstantiationPath()
    throws IOException {
        var builder1 = new DirBuilder(tmp.toPath());
        assertNotNull(builder1);

        var result1 = new StringBuilder();
        var builder2 = new DirBuilder(tmp.toPath(), d -> result1.append("one"));
        assertNotNull(builder2);
        assertEquals(result1.toString(), "one");
    }

    @Test
    void testFileBuilderInstantiation()
    throws IOException {
        var builder1 = new FileBuilder(tmp);
        assertNotNull(builder1);

        var result1 = new StringBuilder();
        var builder2 = new FileBuilder(tmp, f -> result1.append("one"));
        assertNotNull(builder2);
        assertEquals(result1.toString(), "one");
    }

    @Test
    void testFileBuilderInstantiationPath()
    throws IOException {
        var builder1 = new FileBuilder(tmp.toPath());
        assertNotNull(builder1);

        var result1 = new StringBuilder();
        var builder2 = new FileBuilder(tmp.toPath(), f -> result1.append("one"));
        assertNotNull(builder2);
        assertEquals(result1.toString(), "one");
    }

    @Test
    void testFileBuilderPathWithFile() {
        var file = new File("test.txt");
        var builder = new FileBuilder(file);
        var expectedPath = file.getAbsoluteFile().toPath();
        var actualPath = path(file, "subdir", "file.txt");
        assertEquals(expectedPath.resolve("subdir/file.txt"), actualPath);
    }

    @Test
    void testFileBuilderPathWithPath() {
        var path = Path.of("test-files");
        var builder = new FileBuilder(path);
        var expectedPath = path.toAbsolutePath();
        var actualPath = path(path, "subdir", "file.txt");
        assertEquals(expectedPath.resolve("subdir/file.txt"), actualPath);
    }

    @Test
    void testDirBuildDir()
    throws IOException {
        var builder = new DirBuilder(tmp);
        builder.dir("subdir");
        assertTrue(new File(tmp, "subdir").exists());
    }

    @Test
    void testDirBuilderDirWithAction()
    throws IOException {
        var builder = new DirBuilder(tmp, d -> {
            d.dir("subdir");
        });
        assertNotNull(builder);
        assertTrue(new File(tmp, "subdir").exists());
    }

    @Test
    void testDirFileWriteAction()
    throws IOException {
        var builder = new DirBuilder(tmp);
        builder.dir("subdir", d -> d.file("file", f -> f.write("hello")));
        assertTrue(new File(tmp, "subdir").exists());
        assertTrue(new File(new File(tmp, "subdir"), "file").exists());
        assertEquals("hello", FileUtils.readString(new File(new File(tmp, "subdir"), "file")));
    }

    @Test
    void testFileWrite()
    throws IOException {
        var builder = new DirBuilder(tmp);
        builder.file("file", f -> f.write("hello"));
        assertTrue(new File(tmp, "file").exists());
        assertEquals("hello", FileUtils.readString(new File(tmp, "file")));
    }

    @Test
    void testDirDelete()
    throws IOException {
        var builder = new DirBuilder(tmp);
        builder.dir("subdir", d -> d.file("file", f -> f.write("hello")));
        assertTrue(new File(tmp, "subdir").exists());
        assertTrue(new File(new File(tmp, "subdir"), "file").exists());
        builder.dir("subdir", d -> d.delete());
        assertFalse(new File(tmp, "subdir").exists());
        assertFalse(new File(new File(tmp, "subdir"), "file").exists());
        assertFalse(new File(tmp, "file").exists());
    }

    @Test
    void testPermsMode()
    throws IOException {
        var dirBuilder = new DirBuilder(tmp);

        // Test setting read and execute permissions
        var mode = 0555;
        dirBuilder.perms(mode);
        Set<PosixFilePermission> expectedPermissions = new HashSet<>();
        expectedPermissions.add(PosixFilePermission.OWNER_READ);
        expectedPermissions.add(PosixFilePermission.GROUP_READ);
        expectedPermissions.add(PosixFilePermission.OTHERS_READ);
        expectedPermissions.add(PosixFilePermission.OWNER_EXECUTE);
        expectedPermissions.add(PosixFilePermission.GROUP_EXECUTE);
        expectedPermissions.add(PosixFilePermission.OTHERS_EXECUTE);
        assertEquals(expectedPermissions, Files.getPosixFilePermissions(tmp.toPath()));

        // Test setting all permissions
        mode = 0777;
        dirBuilder.perms(mode);
        expectedPermissions = new HashSet<>();
        expectedPermissions.add(PosixFilePermission.OWNER_READ);
        expectedPermissions.add(PosixFilePermission.GROUP_READ);
        expectedPermissions.add(PosixFilePermission.OTHERS_READ);
        expectedPermissions.add(PosixFilePermission.OWNER_WRITE);
        expectedPermissions.add(PosixFilePermission.GROUP_WRITE);
        expectedPermissions.add(PosixFilePermission.OTHERS_WRITE);
        expectedPermissions.add(PosixFilePermission.OWNER_EXECUTE);
        expectedPermissions.add(PosixFilePermission.GROUP_EXECUTE);
        expectedPermissions.add(PosixFilePermission.OTHERS_EXECUTE);
        assertEquals(expectedPermissions, Files.getPosixFilePermissions(tmp.toPath()));
    }

    @Test
    void testPerms()
    throws IOException {
        var dirBuilder = new DirBuilder(tmp);

        // Test setting read and execute permissions
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        dirBuilder.perms(permissions);
        assertEquals(permissions, Files.getPosixFilePermissions(tmp.toPath()));

        // Test setting all permissions
        permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.OTHERS_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        dirBuilder.perms(permissions);
        assertEquals(permissions, Files.getPosixFilePermissions(tmp.toPath()));
    }

    @Test
    void testFileTouch()
    throws IOException {
        var tempFile = File.createTempFile("test", ".txt");
        var fileBuilder = new FileBuilder(tempFile);

        // Test creating a new file
        fileBuilder.delete().touch();
        assertTrue(tempFile.exists());

        // Test updating the last access and last modification time of an existing file
        var now = System.currentTimeMillis();
        fileBuilder.touch();
        var lastAccessTime = (FileTime) Files.getAttribute(tempFile.toPath(), "lastAccessTime");
        assertEquals(now, lastAccessTime.toMillis());
        assertEquals(now, tempFile.lastModified());
    }

    @Test
    void testMoveFile()
    throws IOException {
        var sourceFile = File.createTempFile("source", ".txt");
        var targetFile = File.createTempFile("target", ".txt");
        var fileBuilder = new FileBuilder(targetFile);

        // Test moving a file
        fileBuilder.delete().move(sourceFile.toPath());
        assertFalse(sourceFile.exists());
        assertTrue(targetFile.exists());
    }

    @Test
    void testDeleteFile()
    throws IOException {
        var tempFile = File.createTempFile("test", ".txt");
        var fileBuilder = new FileBuilder(tempFile);

        // Test deleting a file
        fileBuilder.delete();
        assertFalse(tempFile.exists());
    }

    @Test
    void testFilePermsMode()
    throws IOException {
        var tempFile = File.createTempFile("test", ".txt");
        var fileBuilder = new FileBuilder(tempFile);

        // Test setting read and execute permissions
        var mode = 0555;
        fileBuilder.perms(mode);
        Set<PosixFilePermission> expectedPermissions = new HashSet<>();
        expectedPermissions.add(PosixFilePermission.OWNER_READ);
        expectedPermissions.add(PosixFilePermission.GROUP_READ);
        expectedPermissions.add(PosixFilePermission.OTHERS_READ);
        expectedPermissions.add(PosixFilePermission.OWNER_EXECUTE);
        expectedPermissions.add(PosixFilePermission.GROUP_EXECUTE);
        expectedPermissions.add(PosixFilePermission.OTHERS_EXECUTE);
        assertEquals(expectedPermissions, Files.getPosixFilePermissions(tempFile.toPath()));

        // Test setting all permissions
        mode = 0777;
        fileBuilder.perms(mode);
        expectedPermissions = new HashSet<>();
        expectedPermissions.add(PosixFilePermission.OWNER_READ);
        expectedPermissions.add(PosixFilePermission.GROUP_READ);
        expectedPermissions.add(PosixFilePermission.OTHERS_READ);
        expectedPermissions.add(PosixFilePermission.OWNER_WRITE);
        expectedPermissions.add(PosixFilePermission.GROUP_WRITE);
        expectedPermissions.add(PosixFilePermission.OTHERS_WRITE);
        expectedPermissions.add(PosixFilePermission.OWNER_EXECUTE);
        expectedPermissions.add(PosixFilePermission.GROUP_EXECUTE);
        expectedPermissions.add(PosixFilePermission.OTHERS_EXECUTE);
        assertEquals(expectedPermissions, Files.getPosixFilePermissions(tempFile.toPath()));
    }

    @Test
    void testFilePerms()
    throws IOException {
        var tempFile = File.createTempFile("test", ".txt");
        var fileBuilder = new FileBuilder(tempFile);

        // Test setting read and execute permissions
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        fileBuilder.perms(permissions);
        assertEquals(permissions, Files.getPosixFilePermissions(tempFile.toPath()));

        // Test setting all permissions
        permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.OTHERS_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        fileBuilder.perms(permissions);
        assertEquals(permissions, Files.getPosixFilePermissions(tempFile.toPath()));
    }

    @Test
    void testFileAndDirectoryHierarchy()
    throws IOException {
        new DirBuilder(tmp, d -> {
            d.dir("subdir1", s1 -> {
                s1.file("file1.txt", f1 -> f1.write("hello world"));
                s1.file("file2.txt", f2 -> f2.write("hello again"));
            });
            d.dir("subdir2", s2 -> {
                s2.file("file3.txt", f3 -> f3.write("goodbye"));
            });
        });

        var subdir1 = tmp.toPath().resolve("subdir1").toFile();
        assertTrue(subdir1.exists());
        assertTrue(subdir1.isDirectory());
        var file1 = tmp.toPath().resolve("subdir1/file1.txt").toFile();
        assertTrue(file1.exists());
        assertTrue(file1.isFile());
        var file2 = tmp.toPath().resolve("subdir1/file2.txt").toFile();
        assertTrue(file2.exists());
        assertTrue(file2.isFile());

        var subdir2 = tmp.toPath().resolve("subdir2").toFile();
        assertTrue(subdir2.exists());
        assertTrue(subdir2.isDirectory());
        var file3 = tmp.toPath().resolve("subdir2/file3.txt").toFile();
        assertTrue(file3.exists());
        assertTrue(file3.isFile());
    }

    @Test
    void testDirectoryHierarchyCopyMove()
    throws IOException {
        var b = new DirBuilder(tmp, d -> {
            d.dir("subdir1", s1 -> {
                s1.file("file1.txt", f -> {
                    f.write("Hello World!");
                });
                s1.dir("subdir2", s2 -> {
                    s2.file("file2.txt", f -> {
                        f.write("Foo");
                    });
                });
            });
            d.dir("subdir3", s3 -> {
                s3.file("file3.txt", f -> f.move(path(tmp, "subdir1", "subdir2", "file2.txt")));
                s3.file("file4.txt", f -> f.copy(path(tmp, "subdir1", "file1.txt")));
            });
        });

        assertTrue(path(tmp, "subdir1", "file1.txt").toFile().exists());
        assertTrue(path(tmp, "subdir3", "file4.txt").toFile().exists());
        assertFalse(path(tmp, "subdir1", "subdir2", "file2.txt").toFile().exists());
        assertTrue(path(tmp, "subdir3", "file3.txt").toFile().exists());

        assertEquals(Files.readString(path(tmp, "subdir1", "file1.txt")), Files.readString(path(tmp, "subdir3", "file4.txt")));
        assertEquals("Foo", Files.readString(path(tmp, "subdir3", "file3.txt")));
    }

    @Test
    void testDirectoryHierarchyAgain()
    throws Exception {
        var operation = new CreateBlankOperation()
            .workDirectory(tmp)
            .packageName("tst")
            .projectName("tst")
            .downloadDependencies(false);
        operation.execute();

        new DirBuilder(tmp, t -> {
            t.dir("bld", b -> {
                b.dir("bin", i -> {
                    i.file("bld", f -> {
                        f.copy(path(tmp, "tst", "bld"));
                        f.perms(0755);
                    });
                    i.file("bld.bat", f -> {
                        f.copy(path(tmp, "tst", "bld.bat"));
                        f.perms(0755);
                    });
                });
                b.dir("lib", l -> {
                    l.file("bld-wrapper.jar", f -> f.move(path(tmp, "tst", "lib", "bld", "bld-wrapper.jar")));
                });
            });
            t.dir("tst", l -> l.delete());
        });

        assertEquals("""
            /bld
            /bld/bin
            /bld/bin/bld
            /bld/bin/bld.bat
            /bld/lib
            /bld/lib/bld-wrapper.jar""", FileUtils.generateDirectoryListing(tmp));
    }

}
