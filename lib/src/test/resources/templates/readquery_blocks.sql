/*b query1*/SELECT name FROM tbltest WHERE name = '/*v name-*/'/*-b*/
{{b query2}}SELECT name FROM tbltest WHERE name = ?{{/b}}
