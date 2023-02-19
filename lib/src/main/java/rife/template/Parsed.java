/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.net.URL;
import java.util.*;

import rife.asm.ClassWriter;
import rife.asm.Label;
import rife.asm.MethodVisitor;
import rife.asm.Opcodes;
import rife.template.exceptions.TemplateException;

final class Parsed implements Opcodes {
	private Parser parser_ = null;
	private String templateName_ = null;
	private String package_ = null;
	private String className_ = null;
	private URL resource_ = null;
	private long modificationTime_ = -1;

	private final Map<String, ParsedBlockData> blocks_ = new LinkedHashMap<>();
	private final Set<String> valueIds_ = new LinkedHashSet<>();
	private final Map<String, String> defaultValues_ = new HashMap<>();
	private final List<String> blockvalues_ = new ArrayList<>();
	private final Map<URL, Long> dependencies_ = new HashMap<>();
	private String modificationState_ = null;
	private FilteredTagsMap filteredValuesMap_ = null;
	private FilteredTagsMap filteredBlocksMap_ = null;

	Parsed(Parser parser) {
		assert parser != null;

		parser_ = parser;
	}

	private Map<Integer, ArrayList<String>> getHashcodeKeysMapping(Collection<String> stringCollection) {
		// create a mapping of all string hash codes to their possible real values
		// hash codes will be used in a switch for quick lookup of blocks
		Map<Integer, ArrayList<String>> hashcode_keys_mapping = new HashMap<>();
		int hashcode;
		ArrayList<String> keys;
		for (var key : stringCollection) {
			hashcode = key.hashCode();
			keys = hashcode_keys_mapping.computeIfAbsent(hashcode, k -> new ArrayList<>());
			keys.add(key);
		}

		return hashcode_keys_mapping;
	}

	// store an integer on the stack
	private void addIntegerConst(MethodVisitor method, int value) {
		switch (value) {
			case -1 -> method.visitInsn(ICONST_M1);
			case 0 -> method.visitInsn(ICONST_0);
			case 1 -> method.visitInsn(ICONST_1);
			case 2 -> method.visitInsn(ICONST_2);
			case 3 -> method.visitInsn(ICONST_3);
			case 4 -> method.visitInsn(ICONST_4);
			case 5 -> method.visitInsn(ICONST_5);
			default -> method.visitLdcInsn(value);
		}
	}

	byte[] getByteCode() {
		var class_writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		MethodVisitor method = null;

		var full_classname = (getPackage() + "." + getClassName()).replace('.', '/');

		// define the template class
class_writer.visit(V17, ACC_PUBLIC|ACC_SYNCHRONIZED, full_classname, null, "rife/template/AbstractTemplate", null);

		// generate the template constructor
method = class_writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
method.visitVarInsn            (ALOAD, 0);
method.visitMethodInsn         (INVOKESPECIAL, "rife/template/AbstractTemplate", "<init>", "()V", false);
method.visitInsn               (RETURN);
method.visitMaxs               (0, 0);

        // define the method that will return the template name
method = class_writer.visitMethod(ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
method.visitLdcInsn            (className_);
method.visitInsn               (ARETURN);
method.visitMaxs               (0, 0);

		// define the method that will return the full template name
method = class_writer.visitMethod(ACC_PUBLIC, "getFullName", "()Ljava/lang/String;", null, null);
method.visitLdcInsn            (templateName_);
method.visitInsn               (ARETURN);
method.visitMaxs               (0, 0);

		// define the methods that will return the modification time
method = class_writer.visitMethod(ACC_STATIC, "getModificationTimeReal", "()J", null, null);
method.visitLdcInsn            (getModificationTime());
method.visitInsn               (LRETURN);
method.visitMaxs               (0, 0);

method = class_writer.visitMethod(ACC_PUBLIC, "getModificationTime", "()J", null, null);
method.visitMethodInsn         (INVOKESTATIC, full_classname, "getModificationTimeReal", "()J", false);
method.visitInsn               (LRETURN);
method.visitMaxs               (0, 0);

		// define the method that will return the modification state
method = class_writer.visitMethod(ACC_STATIC, "getModificationState", "()Ljava/lang/String;", null, null);
		if (null == modificationState_)  {
method.visitInsn               (ACONST_NULL);
		} else {
method.visitLdcInsn            (modificationState_);
		}
method.visitInsn               (ARETURN);
method.visitMaxs               (0, 0);

		// prepare the blocks for lookup switches
		ParsedBlockData block_data;
		ArrayList<String> keys;

		Map<Integer, ArrayList<String>> hashcode_keys_mapping = null;
		Map<String, ParsedBlockPart> blockparts_order = null;
		int[] hashcodes = null;

		hashcode_keys_mapping = getHashcodeKeysMapping(blocks_.keySet());
		blockparts_order = new LinkedHashMap<>();
		{
            var hashcodes_set = hashcode_keys_mapping.keySet();
			hashcodes = new int[hashcodes_set.size()];
            var hashcode_index = 0;
			for (var i : hashcodes_set) {
				hashcodes[hashcode_index++] = i;
			}
		}
		Arrays.sort(hashcodes);

		// generate the method that will append the block parts according to the current set of values
		// for external usage
		{
method = class_writer.visitMethod(ACC_PROTECTED, "appendBlockExternalForm", "(Ljava/lang/String;Lrife/template/ExternalValue;)Z", null, null);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
			var external_default = new Label();
			var external_found = new Label();
			var external_labels = new Label[hashcodes.length];
			for (var i = 0; i < external_labels.length; i++) {
				external_labels[i] = new Label();
			}

method.visitLookupSwitchInsn   (external_default, hashcodes, external_labels);
			var blockdata_static_prefix = "sBlockPart";
			var blockdata_static_counter = 0L;
			String static_identifier;
			for (var i = 0; i < hashcodes.length; i++) {
method.visitLabel              (external_labels[i]);

				keys = hashcode_keys_mapping.get(hashcodes[i]);
				if (1 == keys.size()) {
					block_data = blocks_.get(keys.get(0));

					var block_data_it = block_data.iterator();
					ParsedBlockPart block_part;
					while (block_data_it.hasNext()) {
						block_part = block_data_it.next();

						static_identifier = blockdata_static_prefix + (blockdata_static_counter++);

						blockparts_order.put(static_identifier, block_part);
block_part.visitByteCodeExternalForm(method, full_classname, static_identifier);
					}
				} else {
					for (var key : keys) {
						var after_key_label = new Label();
method.visitVarInsn            (ALOAD, 1);
method.visitLdcInsn            (key);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
method.visitJumpInsn           (IFEQ, after_key_label);

						block_data = blocks_.get(key);

						for (var block_part : block_data) {
							static_identifier = blockdata_static_prefix + (blockdata_static_counter++);

							blockparts_order.put(static_identifier, block_part);
block_part.visitByteCodeExternalForm(method, full_classname, static_identifier);
						}
method.visitJumpInsn           (GOTO, external_found);
method.visitLabel              (after_key_label);
					}
method.visitInsn               (ICONST_0);
method.visitInsn               (IRETURN);
				}
method.visitJumpInsn           (GOTO, external_found);
			}
method.visitLabel              (external_default);
method.visitInsn               (ICONST_0);
method.visitInsn               (IRETURN);
method.visitLabel              (external_found);
method.visitInsn               (ICONST_1);
method.visitInsn               (IRETURN);
method.visitMaxs               (0, 0);
		}

		// generate the method that will append the block parts according to the current set of values
		// for internal usage
		{
method = class_writer.visitMethod(ACC_PROTECTED, "appendBlockInternalForm", "(Ljava/lang/String;Lrife/template/InternalValue;)Z", null, null);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
			var internal_default = new Label();
			var internal_found = new Label();
			var internal_labels = new Label[hashcodes.length];
			for (var i = 0; i < internal_labels.length; i++) {
				internal_labels[i] = new Label();
			}

method.visitLookupSwitchInsn   (internal_default, hashcodes, internal_labels);
			String static_identifier = null;
			var static_identifiers_it = blockparts_order.keySet().iterator();
			for (var i = 0; i < hashcodes.length; i++) {
method.visitLabel              (internal_labels[i]);

				var text_count = 0;
				var value_count = 0;

				keys = hashcode_keys_mapping.get(hashcodes[i]);
				if (1 == keys.size()) {
					block_data = blocks_.get(keys.get(0));

					Iterator<ParsedBlockPart> block_data_it = null;
					ParsedBlockPart block_part = null;

					block_data_it = block_data.iterator();
					while (block_data_it.hasNext()) {
						block_part = block_data_it.next();

						if (ParsedBlockPart.Type.TEXT == block_part.getType()) {
							text_count++;
						} else if (ParsedBlockPart.Type.VALUE == block_part.getType()) {
							value_count++;
						}
					}

					if (text_count + value_count > 0) {
method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 2);
addIntegerConst                (method, text_count+value_count);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "increasePartsCapacityInternal", "(Lrife/template/InternalValue;I)V", false);
					}

					if (value_count > 0) {
method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 2);
addIntegerConst                (method, value_count);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "increaseValuesCapacityInternal", "(Lrife/template/InternalValue;I)V", false);
					}

					block_data_it = block_data.iterator();
					while (block_data_it.hasNext()) {
						block_part = block_data_it.next();

						static_identifier = static_identifiers_it.next();

						block_part.visitByteCodeInternalForm(method, full_classname, static_identifier);
					}
method.visitJumpInsn           (GOTO, internal_found);
				} else {
					for (var key : keys) {
						var after_key_label = new Label();
method.visitVarInsn            (ALOAD, 1);
method.visitLdcInsn            (key);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
method.visitJumpInsn           (IFEQ, after_key_label);

						block_data = blocks_.get(key);

						Iterator<ParsedBlockPart> block_data_it;
						ParsedBlockPart block_part;

						block_data_it = block_data.iterator();
						while (block_data_it.hasNext()) {
							block_part = block_data_it.next();

							if (ParsedBlockPart.Type.TEXT == block_part.getType()) {
								text_count++;
							} else if (ParsedBlockPart.Type.VALUE == block_part.getType()) {
								value_count++;
							}
						}

method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 2);
addIntegerConst                (method, text_count+value_count);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "increasePartsCapacityInternal", "(Lrife/template/InternalValue;I)V", false);

method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 2);
addIntegerConst                (method, value_count);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "increaseValuesCapacityInternal", "(Lrife/template/InternalValue;I)V", false);

						block_data_it = block_data.iterator();
						while (block_data_it.hasNext()) {
							block_part = block_data_it.next();

							static_identifier = static_identifiers_it.next();

							block_part.visitByteCodeInternalForm(method, full_classname, static_identifier);
						}

method.visitJumpInsn           (GOTO, internal_found);
method.visitLabel              (after_key_label);
					}
method.visitInsn               (ICONST_0);
method.visitInsn               (IRETURN);
				}
			}

method.visitLabel              (internal_default);
method.visitInsn               (ICONST_0);
method.visitInsn               (IRETURN);
method.visitLabel              (internal_found);
method.visitInsn               (ICONST_1);
method.visitInsn               (IRETURN);
method.visitMaxs               (0, 0);
		}

		// generate the method that will return the defined default values
method = class_writer.visitMethod(ACC_PUBLIC, "getDefaultValue", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
		{
			var after_null_check = new Label();
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ALOAD, 1);
method.visitJumpInsn           (IF_ACMPNE, after_null_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("id can't be null.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_null_check);

			var after_empty_check = new Label();
method.visitInsn               (ICONST_0);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
method.visitJumpInsn           (IF_ICMPNE, after_empty_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("id can't be empty.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_empty_check);

method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ASTORE, 2);

			if (blockvalues_.size() > 0) {
				var blockvalue_doesnt_exist_label = new Label();
method.visitFieldInsn          (GETSTATIC, full_classname, "sBlockvalues", "Ljava/util/ArrayList;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/ArrayList", "contains", "(Ljava/lang/Object;)Z", false);
method.visitJumpInsn           (IFEQ, blockvalue_doesnt_exist_label);
method.visitTypeInsn           (NEW, "rife/template/ExternalValue");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "rife/template/ExternalValue", "<init>", "()V", false);
method.visitVarInsn            (ASTORE, 3);
method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 1);
method.visitVarInsn            (ALOAD, 3);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "appendBlockExternalForm", "(Ljava/lang/String;Lrife/template/ExternalValue;)Z", false);
method.visitInsn               (POP);
method.visitVarInsn            (ALOAD, 3);
method.visitMethodInsn         (INVOKEVIRTUAL, "rife/template/ExternalValue", "toString", "()Ljava/lang/String;", false);
method.visitVarInsn            (ASTORE, 2);
method.visitLabel              (blockvalue_doesnt_exist_label);
			}
			if (defaultValues_.size() > 0) {
				var defaultvalues_already_exists_label = new Label();
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ALOAD, 2);
method.visitJumpInsn           (IF_ACMPNE, defaultvalues_already_exists_label);
method.visitFieldInsn          (GETSTATIC, full_classname, "sDefaultValues", "Ljava/util/HashMap;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
method.visitTypeInsn           (CHECKCAST, "java/lang/String");
method.visitVarInsn            (ASTORE, 2);
method.visitLabel              (defaultvalues_already_exists_label);
			}

method.visitVarInsn            (ALOAD, 2);
method.visitInsn               (ARETURN);
method.visitMaxs               (0, 0);
		}

		// generate the method that will append defined default values
		// for external usage
method = class_writer.visitMethod(ACC_PROTECTED, "appendDefaultValueExternalForm", "(Ljava/lang/String;Lrife/template/ExternalValue;)Z", null, null);
		{
method.visitInsn               (ICONST_0);
method.visitVarInsn            (ISTORE, 3);
			if (blockvalues_.size() > 0) {
				var blockvalue_doesnt_exist_label = new Label();
method.visitFieldInsn          (GETSTATIC, full_classname, "sBlockvalues", "Ljava/util/ArrayList;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/ArrayList", "contains", "(Ljava/lang/Object;)Z", false);
method.visitJumpInsn           (IFEQ, blockvalue_doesnt_exist_label);
method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 1);
method.visitVarInsn            (ALOAD, 2);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "appendBlockExternalForm", "(Ljava/lang/String;Lrife/template/ExternalValue;)Z", false);
method.visitInsn               (POP);
method.visitInsn               (ICONST_1);
method.visitVarInsn            (ISTORE, 3);
method.visitLabel              (blockvalue_doesnt_exist_label);
			}
			if (defaultValues_.size() > 0) {
				var already_found_defaultvalue_label = new Label();
method.visitVarInsn            (ILOAD, 3);
method.visitJumpInsn           (IFNE, already_found_defaultvalue_label);
method.visitFieldInsn          (GETSTATIC, full_classname, "sDefaultValues", "Ljava/util/HashMap;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "containsKey", "(Ljava/lang/Object;)Z", false);
method.visitJumpInsn           (IFEQ, already_found_defaultvalue_label);
method.visitVarInsn            (ALOAD, 2);
method.visitFieldInsn          (GETSTATIC, full_classname, "sDefaultValues", "Ljava/util/HashMap;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
method.visitTypeInsn           (CHECKCAST, "java/lang/String");
method.visitMethodInsn         (INVOKEVIRTUAL, "rife/template/ExternalValue", "append", "(Ljava/lang/CharSequence;)V", false);
method.visitInsn               (ICONST_1);
method.visitVarInsn            (ISTORE, 3);
method.visitLabel              (already_found_defaultvalue_label);
			}
method.visitVarInsn            (ILOAD, 3);
method.visitInsn               (IRETURN);
method.visitMaxs               (0, 0);
		}

		// generate the method that will append defined default values
		// for internal usage
method = class_writer.visitMethod(ACC_PROTECTED, "appendDefaultValueInternalForm", "(Ljava/lang/String;Lrife/template/InternalValue;)Z", null, null);
		{
			if (blockvalues_.size() > 0) {
				var blockvalue_doesnt_exist_label = new Label();
method.visitFieldInsn          (GETSTATIC, full_classname, "sBlockvalues", "Ljava/util/ArrayList;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/ArrayList", "contains", "(Ljava/lang/Object;)Z", false);
method.visitJumpInsn           (IFEQ, blockvalue_doesnt_exist_label);
method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 1);
method.visitVarInsn            (ALOAD, 2);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "appendBlockInternalForm", "(Ljava/lang/String;Lrife/template/InternalValue;)Z", false);
method.visitInsn               (POP);
method.visitInsn               (ICONST_1);
method.visitInsn               (IRETURN);
method.visitLabel              (blockvalue_doesnt_exist_label);
			}
method.visitInsn               (ICONST_0);
method.visitInsn               (IRETURN);
method.visitMaxs               (0, 0);
		}

		// generate the method that checks the modification status of this particular template class
method = class_writer.visitMethod(ACC_PUBLIC|ACC_STATIC, "isModified", "(Lrife/resources/ResourceFinder;Ljava/lang/String;)Z", null, null);
method.visitFieldInsn          (GETSTATIC, full_classname, "sResource", "Ljava/net/URL;");
method.visitMethodInsn         (INVOKESTATIC, full_classname, "getModificationTimeReal", "()J", false);
method.visitFieldInsn          (GETSTATIC, full_classname, "sDependencies", "Ljava/util/HashMap;");
method.visitMethodInsn         (INVOKESTATIC, full_classname, "getModificationState", "()Ljava/lang/String;", false);
method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKESTATIC, full_classname, "isTemplateClassModified", "(Ljava/net/URL;JLjava/util/Map;Ljava/lang/String;Lrife/resources/ResourceFinder;Ljava/lang/String;)Z", false);
method.visitInsn               (IRETURN);
method.visitMaxs               (0, 0);

		// generate the method that checks if a value is present in a template
method = class_writer.visitMethod(ACC_PUBLIC, "hasValueId", "(Ljava/lang/String;)Z", null, null);
			if (valueIds_.size() > 0) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sValueIds", "Ljava/util/HashSet;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashSet", "contains", "(Ljava/lang/Object;)Z", false);
			} else {
method.visitInsn               (ICONST_0);
			}
method.visitInsn               (IRETURN);
method.visitMaxs               (0, 0);

		// generate the method that returns all values that are available
method = class_writer.visitMethod(ACC_PUBLIC, "getAvailableValueIds", "()[Ljava/lang/String;", null, null);
			if (valueIds_.size() > 0) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sValueIdsArray", "[Ljava/lang/String;");
			} else {
method.visitInsn               (ICONST_0);
method.visitTypeInsn           (ANEWARRAY, "java/lang/String");
			}
method.visitInsn               (ARETURN);
method.visitMaxs               (0, 0);

			// generate the method that returns all values that aren't set yet
method = class_writer.visitMethod(ACC_PUBLIC, "getUnsetValueIds", "()Ljava/util/Collection;", null, null);
method.visitTypeInsn           (NEW, "java/util/ArrayList");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
method.visitVarInsn            (ASTORE, 1);
			if (valueIds_.size() > 0) {
				var while_start_label = new Label();
				var while_end_label = new Label();
method.visitFieldInsn          (GETSTATIC, full_classname, "sValueIds", "Ljava/util/HashSet;");
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashSet", "iterator", "()Ljava/util/Iterator;", false);
method.visitVarInsn            (ASTORE, 2);
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ASTORE, 3);
method.visitLabel              (while_start_label);
method.visitVarInsn            (ALOAD, 2);
method.visitMethodInsn         (INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
method.visitJumpInsn           (IFEQ, while_end_label);
method.visitVarInsn            (ALOAD, 2);
method.visitMethodInsn         (INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
method.visitTypeInsn           (CHECKCAST, "java/lang/String");
method.visitVarInsn            (ASTORE, 3);
method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 3);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "isValueSet", "(Ljava/lang/String;)Z", false);
method.visitJumpInsn           (IFNE, while_start_label);
method.visitVarInsn            (ALOAD, 0);
method.visitVarInsn            (ALOAD, 3);
method.visitMethodInsn         (INVOKEVIRTUAL, full_classname, "hasDefaultValue", "(Ljava/lang/String;)Z", false);
method.visitJumpInsn           (IFNE, while_start_label);
method.visitVarInsn            (ALOAD, 1);
method.visitVarInsn            (ALOAD, 3);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
method.visitInsn               (POP);
method.visitJumpInsn           (GOTO, while_start_label);
method.visitLabel              (while_end_label);
			}
method.visitVarInsn            (ALOAD, 1);
method.visitInsn               (ARETURN);
method.visitMaxs               (0, 0);

			// generate the method that returns the list of blocks according to a filter
method = class_writer.visitMethod(ACC_PUBLIC, "getFilteredBlocks", "(Ljava/lang/String;)Ljava/util/List;", null, null);
			{
				var after_null_check = new Label();
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ALOAD, 1);
method.visitJumpInsn           (IF_ACMPNE, after_null_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("filter can't be null.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_null_check);

				var after_empty_check = new Label();
method.visitInsn               (ICONST_0);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
method.visitJumpInsn           (IF_ICMPNE, after_empty_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("filter can't be empty.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_empty_check);

				if (filteredBlocksMap_ != null) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sFilteredBlocksMap", "Ljava/util/HashMap;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
method.visitTypeInsn           (CHECKCAST, "java/util/List");
method.visitVarInsn            (ASTORE, 2);
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ALOAD, 2);
                    var list_null_check = new Label();
method.visitJumpInsn           (IF_ACMPNE, list_null_check);
method.visitFieldInsn          (GETSTATIC, "java/util/Collections", "EMPTY_LIST", "Ljava/util/List;");
method.visitVarInsn            (ASTORE, 2);
method.visitLabel              (list_null_check);
method.visitVarInsn            (ALOAD, 2);
method.visitInsn               (ARETURN);
				} else {
method.visitFieldInsn          (GETSTATIC, "java/util/Collections", "EMPTY_LIST", "Ljava/util/List;");
method.visitInsn               (ARETURN);
				}
method.visitMaxs               (0, 0);
			}

			// generate the method that verifies if blocks are present that match a certain filter
method = class_writer.visitMethod(ACC_PUBLIC, "hasFilteredBlocks", "(Ljava/lang/String;)Z", null, null);
			{
				var after_null_check = new Label();
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ALOAD, 1);
method.visitJumpInsn           (IF_ACMPNE, after_null_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("filter can't be null.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_null_check);

				var after_empty_check = new Label();
method.visitInsn               (ICONST_0);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
method.visitJumpInsn           (IF_ICMPNE, after_empty_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("filter can't be empty.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_empty_check);

				if (filteredBlocksMap_ != null) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sFilteredBlocksMap", "Ljava/util/HashMap;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "containsKey", "(Ljava/lang/Object;)Z", false);
method.visitInsn               (IRETURN);
				} else {
method.visitInsn               (ICONST_0);
method.visitInsn               (IRETURN);
				}
method.visitMaxs               (0, 0);
			}

			// generate the method that returns the list of values according to a filter
method = class_writer.visitMethod(ACC_PUBLIC, "getFilteredValues", "(Ljava/lang/String;)Ljava/util/List;", null, null);
			{
				var after_null_check = new Label();
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ALOAD, 1);
method.visitJumpInsn           (IF_ACMPNE, after_null_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("filter can't be null.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_null_check);

				var after_empty_check = new Label();
method.visitInsn               (ICONST_0);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
method.visitJumpInsn           (IF_ICMPNE, after_empty_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("filter can't be empty.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_empty_check);

				if (filteredValuesMap_ != null) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sFilteredValuesMap", "Ljava/util/HashMap;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
method.visitTypeInsn           (CHECKCAST, "java/util/List");
method.visitVarInsn            (ASTORE, 2);
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ALOAD, 2);
					var list_null_check = new Label();
method.visitJumpInsn           (IF_ACMPNE, list_null_check);
method.visitFieldInsn          (GETSTATIC, "java/util/Collections", "EMPTY_LIST", "Ljava/util/List;");
method.visitVarInsn            (ASTORE, 2);
method.visitLabel              (list_null_check);
method.visitVarInsn            (ALOAD, 2);
				} else {
method.visitFieldInsn          (GETSTATIC, "java/util/Collections", "EMPTY_LIST", "Ljava/util/List;");
				}
method.visitInsn               (ARETURN);
method.visitMaxs               (0, 0);
			}

			// generate the method that verifies if values are present that match a certain filter
method = class_writer.visitMethod(ACC_PUBLIC, "hasFilteredValues", "(Ljava/lang/String;)Z", null, null);
			{
				var after_null_check = new Label();
method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ALOAD, 1);
method.visitJumpInsn           (IF_ACMPNE, after_null_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("filter can't be null.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_null_check);

				var after_empty_check = new Label();
method.visitInsn               (ICONST_0);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
method.visitJumpInsn           (IF_ICMPNE, after_empty_check);
method.visitTypeInsn           (NEW, "java/lang/IllegalArgumentException");
method.visitInsn               (DUP);
method.visitLdcInsn            ("filter can't be empty.");
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false);
method.visitInsn               (ATHROW);
method.visitLabel              (after_empty_check);

				if (filteredValuesMap_ != null) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sFilteredValuesMap", "Ljava/util/HashMap;");
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "containsKey", "(Ljava/lang/Object;)Z", false);
method.visitInsn               (IRETURN);
				} else {
method.visitInsn               (ICONST_0);
method.visitInsn               (IRETURN);
				}
method.visitMaxs               (0, 0);
			}

			// generate the method that returns the dependencies
method = class_writer.visitMethod(1, "getDependencies", "()Ljava/util/Map;", null, null);
method.visitFieldInsn          (GETSTATIC, full_classname, "sDependencies", "Ljava/util/HashMap;");
method.visitInsn               (ARETURN);
method.visitMaxs               (0, 0);

			// performs all the static initialization
class_writer.visitField(ACC_PRIVATE|ACC_STATIC, "sResource", "Ljava/net/URL;", null, null);
class_writer.visitField(ACC_PRIVATE|ACC_STATIC, "sDependencies", "Ljava/util/HashMap;", null, null);
			for (var entry : blockparts_order.entrySet()) {
entry.getValue().visitByteCodeStaticDeclaration(class_writer, entry.getKey());
			}
			if (defaultValues_.size() > 0) {
class_writer.visitField(ACC_PRIVATE|ACC_STATIC, "sDefaultValues", "Ljava/util/HashMap;", null, null);
			}
			if (blockvalues_.size() > 0) {
class_writer.visitField(ACC_PRIVATE|ACC_STATIC, "sBlockvalues", "Ljava/util/ArrayList;", null, null);
			}
			if (valueIds_.size() > 0) {
class_writer.visitField(ACC_PRIVATE|ACC_STATIC, "sValueIds", "Ljava/util/HashSet;", null, null);
class_writer.visitField(ACC_PRIVATE|ACC_STATIC, "sValueIdsArray", "[Ljava/lang/String;", null, null);
			}
			if (filteredBlocksMap_ != null) {
class_writer.visitField(ACC_PRIVATE|ACC_STATIC, "sFilteredBlocksMap", "Ljava/util/HashMap;", null, null);
			}
			if (filteredValuesMap_ != null) {
class_writer.visitField(ACC_PRIVATE|ACC_STATIC, "sFilteredValuesMap", "Ljava/util/HashMap;", null, null);
			}

			// static initialization
method = class_writer.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);

			// set the resource
		var resource_start_label = new Label();
		var resource_end_label = new Label();
		var resource_handler_label = new Label();
		var after_resource_label = new Label();
method.visitLabel              (resource_start_label);
method.visitTypeInsn           (NEW, "java/net/URL");
method.visitInsn               (DUP);
method.visitLdcInsn            (resource_.getProtocol());
method.visitLdcInsn            (resource_.getHost());
addIntegerConst(method, resource_.getPort());
method.visitLdcInsn            (resource_.getFile());
method.visitMethodInsn         (INVOKESPECIAL, "java/net/URL", "<init>", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V", false);
method.visitFieldInsn          (PUTSTATIC, full_classname, "sResource", "Ljava/net/URL;");
method.visitLabel              (resource_end_label);
method.visitJumpInsn           (GOTO, after_resource_label);
method.visitLabel              (resource_handler_label);
method.visitVarInsn            (ASTORE, 0);
method.visitInsn               (ACONST_NULL);
method.visitFieldInsn          (PUTSTATIC, full_classname, "sResource", "Ljava/net/URL;");
method.visitTryCatchBlock      (resource_start_label, resource_end_label, resource_handler_label, "java/net/MalformedURLException");
method.visitLabel              (after_resource_label);

			// set the file dependencies
method.visitTypeInsn           (NEW, "java/util/HashMap");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
method.visitFieldInsn          (PUTSTATIC, full_classname, "sDependencies", "Ljava/util/HashMap;");
			if (dependencies_.size() > 0) {
				for (var url : dependencies_.keySet()) {
					var url_start_label = new Label();
					var url_end_label = new Label();
					var url_handler_label = new Label();
					var after_url_label = new Label();
method.visitLabel              (url_start_label);
method.visitFieldInsn          (GETSTATIC, full_classname, "sDependencies", "Ljava/util/HashMap;");
method.visitTypeInsn           (NEW, "java/net/URL");
method.visitInsn               (DUP);
method.visitLdcInsn            (url.getProtocol());
method.visitLdcInsn            (url.getHost());
addIntegerConst(method, url.getPort());
method.visitLdcInsn            (url.getFile());
method.visitMethodInsn         (INVOKESPECIAL, "java/net/URL", "<init>", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V", false);
method.visitTypeInsn           (NEW, "java/lang/Long");
method.visitInsn               (DUP);
method.visitLdcInsn            (dependencies_.get(url));
method.visitMethodInsn         (INVOKESPECIAL, "java/lang/Long", "<init>", "(J)V", false);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
method.visitInsn               (POP);
method.visitLabel              (url_end_label);
method.visitJumpInsn           (GOTO, after_url_label);
method.visitLabel              (url_handler_label);
method.visitVarInsn            (ASTORE, 0);
method.visitTryCatchBlock      (url_start_label, url_end_label, url_handler_label, "java/net/MalformedURLException");
method.visitLabel              (after_url_label);
				}
			}

			// generate the static initialization for the block data
			for (var entry : blockparts_order.entrySet()) {
entry.getValue().visitByteCodeStaticDefinition(method, full_classname, entry.getKey());
			}

			// set the default values if they're present
			if (defaultValues_.size() > 0) {
method.visitTypeInsn           (NEW, "java/util/HashMap");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
method.visitFieldInsn          (PUTSTATIC, full_classname, "sDefaultValues", "Ljava/util/HashMap;");
				for (var key : defaultValues_.keySet()) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sDefaultValues", "Ljava/util/HashMap;");
method.visitLdcInsn            (key);
method.visitLdcInsn            (defaultValues_.get(key));
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
method.visitInsn               (POP);
				}
			}

			// set the blockvalues if they're present
			if (blockvalues_.size() > 0) {
method.visitTypeInsn           (NEW, "java/util/ArrayList");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
method.visitFieldInsn          (PUTSTATIC, full_classname, "sBlockvalues", "Ljava/util/ArrayList;");
				for (var key : blockvalues_) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sBlockvalues", "Ljava/util/ArrayList;");
method.visitLdcInsn            (key);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
method.visitInsn               (POP);
				}
			}

			// set the values ids if they're present
			if (valueIds_.size() > 0) {
method.visitTypeInsn           (NEW, "java/util/HashSet");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false);
method.visitFieldInsn          (PUTSTATIC, full_classname, "sValueIds", "Ljava/util/HashSet;");
				for (var id : valueIds_) {
method.visitFieldInsn          (GETSTATIC, full_classname, "sValueIds", "Ljava/util/HashSet;");
method.visitLdcInsn            (id);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)Z", false);
method.visitInsn               (POP);
				}
method.visitFieldInsn          (GETSTATIC, full_classname, "sValueIds", "Ljava/util/HashSet;");
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashSet", "size", "()I", false);
method.visitTypeInsn           (ANEWARRAY, "java/lang/String");
method.visitFieldInsn          (PUTSTATIC, full_classname, "sValueIdsArray", "[Ljava/lang/String;");
method.visitFieldInsn          (GETSTATIC, full_classname, "sValueIds", "Ljava/util/HashSet;");
method.visitFieldInsn          (GETSTATIC, full_classname, "sValueIdsArray", "[Ljava/lang/String;");
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashSet", "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;", false);
method.visitInsn               (POP);
			}

			// write the filtered blocks if they're present
			if (filteredBlocksMap_ != null) {
method.visitTypeInsn           (NEW, "java/util/HashMap");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
method.visitFieldInsn          (PUTSTATIC, full_classname, "sFilteredBlocksMap", "Ljava/util/HashMap;");

method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ASTORE, 0);
				FilteredTags filtered_blocks;

				for (var key : filteredBlocksMap_.keySet()) {
					filtered_blocks = filteredBlocksMap_.getFilteredTag(key);
method.visitTypeInsn           (NEW, "java/util/ArrayList");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
method.visitVarInsn            (ASTORE, 1);

					for (var captured_groups : filtered_blocks) {
method.visitVarInsn            (ALOAD, 1);
addIntegerConst(method, captured_groups.length);
method.visitTypeInsn           (ANEWARRAY, "java/lang/String");
						for (var i = 0; i < captured_groups.length; i++) {
method.visitInsn               (DUP);
addIntegerConst(method, i);
method.visitLdcInsn            (captured_groups[i]);
method.visitInsn               (AASTORE);
						}
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
method.visitInsn               (POP);
					}
method.visitFieldInsn          (GETSTATIC, full_classname, "sFilteredBlocksMap", "Ljava/util/HashMap;");
method.visitLdcInsn            (key);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn		   (INVOKESTATIC, "java/util/Collections", "unmodifiableList", "(Ljava/util/List;)Ljava/util/List;", false);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
method.visitInsn               (POP);
				}
			}

			// write the filtered values if they're present
			if (filteredValuesMap_ != null) {
method.visitTypeInsn           (NEW, "java/util/HashMap");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
method.visitFieldInsn          (PUTSTATIC, full_classname, "sFilteredValuesMap", "Ljava/util/HashMap;");

method.visitInsn               (ACONST_NULL);
method.visitVarInsn            (ASTORE, 1);
				FilteredTags filtered_values;

				for (var key : filteredValuesMap_.keySet()) {
					filtered_values = filteredValuesMap_.getFilteredTag(key);
method.visitTypeInsn           (NEW, "java/util/ArrayList");
method.visitInsn               (DUP);
method.visitMethodInsn         (INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
method.visitVarInsn            (ASTORE, 1);

					for (var captured_groups : filtered_values) {
method.visitVarInsn            (ALOAD, 1);
addIntegerConst(method, captured_groups.length);
method.visitTypeInsn           (ANEWARRAY, "java/lang/String");
						for (var i = 0; i < captured_groups.length; i++) {
method.visitInsn               (DUP);
addIntegerConst(method, i);
							if (null == captured_groups[i]) {
method.visitInsn               (ACONST_NULL);
							} else {
method.visitLdcInsn            (captured_groups[i]);
							}
method.visitInsn               (AASTORE);
						}
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
method.visitInsn               (POP);
					}
method.visitFieldInsn          (GETSTATIC, full_classname, "sFilteredValuesMap", "Ljava/util/HashMap;");
method.visitLdcInsn            (key);
method.visitVarInsn            (ALOAD, 1);
method.visitMethodInsn		   (INVOKESTATIC, "java/util/Collections", "unmodifiableList", "(Ljava/util/List;)Ljava/util/List;", false);
method.visitMethodInsn         (INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
method.visitInsn               (POP);
				}
			}
method.visitInsn               (RETURN);
method.visitMaxs               (0, 0);

class_writer.visitEnd();

		return class_writer.toByteArray();
	}

	void setTemplateName(String templateName) {
		assert templateName != null;

		templateName_ = templateName;
	}

	String getTemplateName() {
		return templateName_;
	}

	void setClassName(String className) {
		assert className != null;

		className_ = className;
	}

	String getClassName() {
		return className_;
	}

	String getFullClassName() {
		if (null != className_) {
			return package_ + "." + className_;
		} else {
			return null;
		}
	}

	void setResource(URL resource) {
		assert resource != null;

		resource_ = resource;
	}

	URL getResource() {
		return resource_;
	}

	private long getModificationTime()
	throws TemplateException {
		if (-1 == modificationTime_) {
			modificationTime_ = Parser.getModificationTime(parser_.getTemplateFactory().getResourceFinder(), getResource());
		}

		assert modificationTime_ > 0;

		return modificationTime_;
	}

	void setBlock(String id, ParsedBlockData blockData) {
		assert id != null;
		assert blockData != null;

		blocks_.put(id, blockData);
	}

	ParsedBlockData getContent() {
		return getBlock("");
	}

	ParsedBlockData getBlock(String id) {
		assert id != null;

		return blocks_.get(id);
	}

	Collection<String> getBlockIds() {
		return blocks_.keySet();
	}

	Map<String, ParsedBlockData> getBlocks() {
		return blocks_;
	}

	void addValue(String id) {
		assert id != null;
		assert id.length() > 0;

		valueIds_.add(id);
	}

	Collection<String> getValueIds() {
		return valueIds_;
	}

	void setDefaultValue(String id, String value) {
		assert id != null;
		assert id.length() > 0;
		assert value != null;

		defaultValues_.put(id, value);
	}

	void setBlockvalue(String id) {
		assert id != null;
		assert id.length() > 0;

		blockvalues_.add(id);
	}

	void removeBlockvalue(String id) {
		assert id != null;
		assert id.length() > 0;

		blockvalues_.remove(id);
	}

	String getDefaultValue(String id) {
		assert id != null;
		assert id.length() > 0;

		return defaultValues_.get(id);
	}

	boolean hasDefaultValue(String id) {
		assert id != null;
		assert id.length() > 0;

		return defaultValues_.containsKey(id);
	}

	boolean hasBlockvalue(String id) {
		assert id != null;
		assert id.length() > 0;

		return blockvalues_.contains(id);
	}

	Map<String, String> getDefaultValues() {
		return defaultValues_;
	}

	List<String> getBlockvalues() {
		return blockvalues_;
	}

	void addDependency(Parsed parsed)
	throws TemplateException {
		assert parsed != null;

		long modification_time = -1;

		// store the filename in the array of dependent files
		try {
			modification_time = parsed.getModificationTime();
		} catch (TemplateException e) {
			// set the modification time to -1, this means that the dependent file will be considered
			// as outdated at the next verification
			modification_time = -1;
		}
		dependencies_.put(parsed.getResource(), modification_time);
	}

	void addDependency(URL resource, Long modificationTime) {
		dependencies_.put(resource, modificationTime);
	}

	Map<URL, Long> getDependencies() {
		return dependencies_;
	}

	void setModificationState(String state) {
		modificationState_ = state;
	}

	String getPackage() {
		return package_;
	}

	void setPackage(String thePackage) {
		assert thePackage != null;

		package_ = thePackage;
	}

	void setFilteredValues(FilteredTagsMap filteredValues) {
		filteredValuesMap_ = filteredValues;
	}

	FilteredTagsMap getFilteredValuesMap() {
		return filteredValuesMap_;
	}

	void setFilteredBlocks(FilteredTagsMap filteredBlocks) {
		filteredBlocksMap_ = filteredBlocks;
	}

	FilteredTagsMap getFilteredBlocksMap() {
		return filteredBlocksMap_;
	}
}


