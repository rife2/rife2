// Generated from java-escape by ANTLR 4.11.1

    package rife.template.antlr;

import rife.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TemplateMainParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TemplateMainParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#document}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocument(TemplateMainParser.DocumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#blockContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockContent(TemplateMainParser.BlockContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#valueContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueContent(TemplateMainParser.ValueContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#tagV}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagV(TemplateMainParser.TagVContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#tagVDefault}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagVDefault(TemplateMainParser.TagVDefaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#tagB}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagB(TemplateMainParser.TagBContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#tagBV}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagBV(TemplateMainParser.TagBVContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#tagBA}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagBA(TemplateMainParser.TagBAContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#blockData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockData(TemplateMainParser.BlockDataContext ctx);
	/**
	 * Visit a parse tree produced by {@link TemplateMainParser#valueData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueData(TemplateMainParser.ValueDataContext ctx);
}