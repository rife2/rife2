// Generated from java-escape by ANTLR 4.11.1

    package rife.template.antlr;

import rife.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TemplatePreParser}.
 */
public interface TemplatePreParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TemplatePreParser#document}.
	 * @param ctx the parse tree
	 */
	void enterDocument(TemplatePreParser.DocumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplatePreParser#document}.
	 * @param ctx the parse tree
	 */
	void exitDocument(TemplatePreParser.DocumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplatePreParser#tagI}.
	 * @param ctx the parse tree
	 */
	void enterTagI(TemplatePreParser.TagIContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplatePreParser#tagI}.
	 * @param ctx the parse tree
	 */
	void exitTagI(TemplatePreParser.TagIContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplatePreParser#tagC}.
	 * @param ctx the parse tree
	 */
	void enterTagC(TemplatePreParser.TagCContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplatePreParser#tagC}.
	 * @param ctx the parse tree
	 */
	void exitTagC(TemplatePreParser.TagCContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplatePreParser#docData}.
	 * @param ctx the parse tree
	 */
	void enterDocData(TemplatePreParser.DocDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplatePreParser#docData}.
	 * @param ctx the parse tree
	 */
	void exitDocData(TemplatePreParser.DocDataContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplatePreParser#commentData}.
	 * @param ctx the parse tree
	 */
	void enterCommentData(TemplatePreParser.CommentDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplatePreParser#commentData}.
	 * @param ctx the parse tree
	 */
	void exitCommentData(TemplatePreParser.CommentDataContext ctx);
}