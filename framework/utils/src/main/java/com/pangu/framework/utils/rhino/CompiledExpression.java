package com.pangu.framework.utils.rhino;

import org.mozilla.javascript.Script;

/**
 * 已编译公式对象
 * @author Ramon
 */
public class CompiledExpression {

	public CompiledExpression(String expression, Script scriptObject) {
		this.expression = expression;
		this.scriptObject = scriptObject;
	}

	private String expression;

	private Script scriptObject;

	public String getExpression() {
		return expression;
	}

	public Script getScriptObject() {
		return scriptObject;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompiledExpression other = (CompiledExpression) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Expression -> " + expression + ";";
	}

}
