/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package rife.antlr.v4.runtime.tree.xpath;

import rife.antlr.v4.runtime.tree.ParseTree;
import rife.antlr.v4.runtime.tree.Trees;

import java.util.ArrayList;
import java.util.Collection;

public class XPathWildcardAnywhereElement extends XPathElement {
	public XPathWildcardAnywhereElement() {
		super(XPath.WILDCARD);
	}

	@Override
	public Collection<ParseTree> evaluate(ParseTree t) {
		if ( invert ) return new ArrayList<ParseTree>(); // !* is weird but valid (empty)
		return Trees.getDescendants(t);
	}
}
