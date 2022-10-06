/*b QUERY*/SELECT/*v HINT*//*-v*//*v DISTINCT*//*-v*/ /*v FIELDS-*//*v FROM*//*-v*//*v JOINS*//*-v*//*v WHERE*//*-v*//*v GROUPBY*//*-v*//*v HAVING*//*-v*//*v UNION*//*-v*//*v ORDERBY*//*-v*//*v LIMIT*//*-v*//*-b*/
/*b HINT*/ /*+ /*v EXPRESSION-*/ *//*-b*/
/*b SEPERATOR*/, /*-b*/
/*b FROM*/ FROM /*v TABLE-*//*-b*/
/*b DISTINCT*/ DISTINCT/*-b*/
/*b DISTINCTON*//*-b*/
/*b ALLFIELDS*/*/*-b*/
/*b JOIN_DEFAULT*/, /*v TABLE-*//*-b*/
/*b JOIN_CROSS*/ CROSS JOIN /*v TABLE-*//*-b*/
/*b JOIN_INNER*/ /*v JOIN_INNER_NATURAL*//*-v*/INNER JOIN /*v TABLE-*//*v JOIN_INNER_ON*//*-v*//*v JOIN_INNER_USING*//*-v*//*-b*/
/*b JOIN_INNER_NATURAL*/NATURAL /*-b*/
/*b JOIN_INNER_ON*/ ON (/*v EXPRESSION-*/)/*-b*/
/*b JOIN_INNER_USING*/ USING (/*v EXPRESSION-*/)/*-b*/
/*b JOIN_OUTER*/ /*v JOIN_OUTER_NATURAL*//*-v*//*v JOIN_OUTER_TYPE*//*-v*/OUTER JOIN /*v TABLE-*//*v JOIN_OUTER_ON*//*-v*//*v JOIN_OUTER_USING*//*-v*//*-b*/
/*b JOIN_OUTER_NATURAL*/NATURAL /*-b*/
/*b JOIN_OUTER_ON*/ ON (/*v EXPRESSION-*/)/*-b*/
/*b JOIN_OUTER_USING*/ USING (/*v EXPRESSION-*/)/*-b*/
/*b JOIN_OUTER_FULL*/FULL /*-b*/
/*b JOIN_OUTER_LEFT*/LEFT /*-b*/
/*b JOIN_OUTER_RIGHT*/RIGHT /*-b*/
/*b WHERE*/ WHERE /*v CONDITION-*//*-b*/
/*b GROUPBY*/ GROUP BY /*v EXPRESSION-*//*-b*/
/*b HAVING*/ HAVING /*v EXPRESSION-*//*-b*/
/*b UNION*/ UNION /*v EXPRESSION-*//*-b*/
/*b UNION_ALL*/ UNION ALL /*v EXPRESSION-*//*-b*/
/*b ORDERBY*/ ORDER BY /*v ORDERBY_PARTS-*//*-b*/
/*b ORDERBY_PART*//*v COLUMN-*/ /*v DIRECTION-*//*-b*/
/*b ORDERBY_ASC*/ASC/*-b*/
/*b ORDERBY_DESC*/DESC/*-b*/
/*b LIMIT*//*-b*/
/*b OFFSET*//*-b*/
