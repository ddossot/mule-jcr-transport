package org.mule.providers.jcr.filters;

import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

/**
 * This filter is used as a pattern holder that is passed to the JCR container.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrPropertyNameFilter implements UMOFilter {

	private String pattern = null;

	public boolean accept(UMOMessage message) {
		return true;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}
