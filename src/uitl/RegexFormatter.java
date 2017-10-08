package uitl;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.text.DefaultFormatter;

/*
 * A regular expression based implementation of 
 * AbstractFormatter 
 */
@SuppressWarnings("serial")
public class RegexFormatter extends DefaultFormatter{
	private Pattern pattern;
	private Matcher matcher;
	public RegexFormatter(){
		super();
	}
	  /**
	   * Creates a regular expression based AbstractFormatter.
	   * pattern specifies the regular expression that will be used
	   * to determine if a value is legal.
	   */
	public RegexFormatter(String pattern)throws PatternSyntaxException{
		this();
		setPattern(Pattern.compile(pattern));
	}
	public RegexFormatter(Pattern pattern){
		this();
		setPattern(pattern);
	}
	//重写valueToString()方法
//	public String valueToString(Object value){
//		return "";
//	}
	//重写stringToValue()方法
	  /**
	   * Parses text returning an arbitrary Object. Some formatters
	   * may return null.
	   * 
	   * If a Pattern has been specified and the text completely
	   * matches the regular expression this will invoke setMatcher.
	   * 
	   * @throws ParseException
	   *           if there is an error in the conversion
	   * @param text
	   *          String to convert
	   * @return Object representation of text
	   */
	public Object stringToValue(String txt) throws ParseException{
		Pattern pattern = getPattern();
		if(pattern!=null){
			Matcher matcher = pattern.matcher(txt);
			if(matcher.matches()){
				setMatcher(matcher);
				return super.stringToValue(txt);
			}
			throw new ParseException("Pattern did not match",0);
		}
		return txt;
	}
	
	
	public void setPattern(Pattern pattern){
		this.pattern = pattern;
	}
	public Pattern getPattern(){
		return pattern;
	}
	protected void setMatcher(Matcher matcher){
		this.matcher = matcher;
	}
	protected Matcher getMatcher(){
		return matcher;
	}
}
