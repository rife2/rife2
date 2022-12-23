// Generated from java-escape by ANTLR 4.11.1

    package rife.template.antlr;

import rife.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TemplatePreParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TemplatePreParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TemplatePreParser#document}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocument(TemplatePreParser.DocumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplatePreParser#tagI}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagI(TemplatePreParser.TagIContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplatePreParser#tagC}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagC(TemplatePreParser.TagCContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplatePreParser#docData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocData(TemplatePreParser.DocDataContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplatePreParser#commentData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentData(TemplatePreParser.CommentDataContext ctx);
}