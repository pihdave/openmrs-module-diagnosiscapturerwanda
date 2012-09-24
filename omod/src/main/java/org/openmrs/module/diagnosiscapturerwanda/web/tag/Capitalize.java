package org.openmrs.module.diagnosiscapturerwanda.web.tag;

import java.io.IOException;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Formats objects for display
 */
public class Capitalize extends TagSupport {

	public static final long serialVersionUID = 1L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private Object value;

	/**
	 * @see TagSupport#doStartTag()
	 */
	public int doStartTag() {
		
		String result = WordUtils.capitalizeFully((String)value);

		try {
			pageContext.getOut().write(result);
		} 
		catch (IOException e) {
			log.error("Failed to write to pageContext.getOut()", e);
		}
		
		return SKIP_BODY;
	}

	/**
	 * @see TagSupport#doEndTag()
	 */
	public int doEndTag() {
		value = null;
		return EVAL_PAGE;
	}

	/**
	 * @return the object
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param object the object to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}
