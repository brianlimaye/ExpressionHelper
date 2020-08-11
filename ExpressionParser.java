import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.StringBuilder;
import java.util.Collections;
import java.io.PrintStream;

public class ExpressionParser
{
	final static int MAXSIZES = 18;

	Map<String, List<String>> lookupTable = new HashMap<String, List<String>>();

	List<String> keywords = new ArrayList<String>();

	List<String> betweenRule = new ArrayList<String>();
	List<String> greaterRule = new ArrayList<String>();
	List<String> greaterEqualRule = new ArrayList<String>();
	List<String> lessRule = new ArrayList<String>();
	List<String> lessEqualRule = new ArrayList<String>();
	List<String> equalRule = new ArrayList<String>();

	public void fillKeywords()
	{
		keywords.add("between");
		keywords.add("greater");
		keywords.add("greaterEqual");
		keywords.add("less");
		keywords.add("lessEqual");
		keywords.add("equals");
	}

	public void fillBetweenRule()
	{
		betweenRule.add("starter");
		betweenRule.add("is");
		betweenRule.add("between");
		betweenRule.add("x");
		betweenRule.add("and");
		betweenRule.add("x");

		lookupTable.put("between", betweenRule);
	}

	public void fillGreaterRule()
	{
		greaterRule.add("size");
		greaterRule.add("is");
		greaterRule.add("greater");
		greaterRule.add("than");
		greaterRule.add("x");

		lookupTable.put("greater", greaterRule);
	}

	public void fillGreaterEqualRule()
	{
		greaterEqualRule.add("size");
		greaterEqualRule.add("is");
		greaterEqualRule.add("greater");
		greaterEqualRule.add("than");
		greaterEqualRule.add("or");
		greaterEqualRule.add("equal");
		greaterEqualRule.add("to");
		greaterEqualRule.add("x");

		lookupTable.put("greaterEqual", greaterEqualRule);
	}

	public void fillLessRule()
	{
		lessRule.add("size");
		lessRule.add("is");
		lessRule.add("less");
		lessRule.add("than");
		lessRule.add("x");

		lookupTable.put("less", lessRule);
	}

	public void fillLessEqualRule()
	{
		lessEqualRule.add("size");
		lessEqualRule.add("is");
		lessEqualRule.add("less");
		lessEqualRule.add("than");
		lessEqualRule.add("or");
		lessEqualRule.add("equal");
		lessEqualRule.add("to");
		lessEqualRule.add("x");

		lookupTable.put("lessEqual", lessEqualRule);
	}

	public void fillEqualRule()
	{
		equalRule.add("size");
		equalRule.add("equals");
		equalRule.add("x");

		lookupTable.put("equals", equalRule);
	}

	private List<String> splitExpression(String expression)
	{
		return Arrays.asList(expression.split(" "));
	}

	private String matchRule(List<String> input)
	{
		assert keywords.size() == lookupTable.size();

		String keyword = "";
		boolean result = false;
		String ruleName = null;

		for(int i=0; i< lookupTable.size(); i++)
		{
			keyword = keywords.get(i);
			result = checkRule(input, lookupTable.get(keyword));

			if(result)
			{
				ruleName = keyword;
			}			
		}
		return ruleName;
	}

	private boolean checkRule(List<String> input, List<String> rule)
	{
		List<Double> digits = new ArrayList<Double>();
		List<Double> sortedDigits = new ArrayList<Double>();

		int count = 0;

		boolean checkedSize = false;
		boolean result = false;

		String inputElement = "";
		String ruleElement = "";

		loop:
		for(int j=0; j< rule.size(); j++)
		{
			if((!checkedSize) && (input.size() != rule.size()))
			{
				continue loop;
			}
			
			else
			{
				inputElement = input.get(j).trim();
				ruleElement = rule.get(j).trim();
			}
			
			if(ruleElement.equals("x"))
			{
				if(!isNumeric(inputElement))
				{
					continue loop;
				}
				else
				{
					digits.add(Double.parseDouble(inputElement));
					sortedDigits.add(Double.parseDouble(inputElement));
				}
			}

			else if(ruleElement.equals(rule.get(0)))
			{
				if((!inputElement.equalsIgnoreCase("size")) && (!inputElement.equalsIgnoreCase("style")))
				{
					continue loop;
				}
			}

			else if(!ruleElement.equalsIgnoreCase(inputElement))
			{
				continue loop;
			}
			
			else
			{
				checkedSize = true;
			}

			count++;
	    }

	    Collections.sort(sortedDigits);

	    if(!sortedDigits.equals(digits))
	    {
	    	return false;
	    }
	  
	  	if(count == rule.size())
	  	{
	  		result = true;
	  	}
	  	
	  	return result;
	}

	private static String removeExtraSpaces(String sizeExpression)
	{
		if (sizeExpression == null)
		{
			return null;
		}

		final String[] parts = sizeExpression.split("\\s+");
		final StringBuilder builder = new StringBuilder();
		for (int i=0; i < parts.length; i++)
		{
			builder.append(parts[i].trim());
			if (i != parts.length-1)
			{
				builder.append(" ");
			}
		}
		return builder.toString();
	}
	
	public static boolean isNumeric(String str) { 
	  try {  
	    Double.parseDouble(str);  
	    return true;
	  } catch(NumberFormatException e){  
	    return false;  
	  }  
	}

	public String parseSize(List<String> list, String rule)
	{
		if(rule == null)
		{
			return null;
		}

		return buildBetweenExpression(list, rule);
	}

	private String buildBetweenExpression(List<String> list, String rule)
	{
		if(!lookupTable.containsKey(rule))
		{
			return null;
		}

		boolean isGreater = false;
		boolean isLess = false;
		String element = "";
		StringBuilder expressionBuilder = new StringBuilder();
		String sign1 = "";
		String sign2 = "";

		expressionBuilder.append("(");

		if(rule.contains("less"))
		{
			expressionBuilder.append("size > 0.0) && (");
		}

		for(int i=0; i< list.size(); i++)
		{
			element = list.get(i);
			element = element.toLowerCase();

			switch(element)
			{
				case "size":
					expressionBuilder.append(element + " ");
					break;
				case "style":
					expressionBuilder.append(element + " ");
					break;
				case "between":
					sign1 = ">";
					sign2 = "<";
					expressionBuilder.append(sign1 + " ");
					break;
				case "equals":
					sign1 = "==";
					expressionBuilder.append(sign1 + " ");
					break;
				case "greater":
					isGreater = true;
					sign1 = ">";
					if(!list.contains("equal"))
					{
						expressionBuilder.append(sign1 + " ");
					}
					break;
				case "less":
					isLess = true;
					sign1 = "<";
					expressionBuilder.append(sign1 + " ");
					break;
				case "equal":
					sign1 += "=";
					expressionBuilder.append(sign1 + " ");
					break;
				case "and":
					expressionBuilder.append(" && (size " + sign2 + " ");
					break;
			}

			if(isNumeric(element))
			{
				expressionBuilder.append(element + ")");
			}
		}

		return expressionBuilder.toString();
	}

	public static void main(String[] args)
	{
		ExpressionParser ep = new ExpressionParser();

		ep.fillKeywords();
		ep.fillBetweenRule();
		ep.fillGreaterRule();
		ep.fillGreaterEqualRule();
		ep.fillLessRule();
		ep.fillLessEqualRule();
		ep.fillEqualRule();

		Scanner sc = new Scanner(System.in);
		String input = "";

		while(true)
		{
			System.out.println("<<<Please enter in a range of sizes.");
			input = sc.nextLine();
			System.out.println();
			List<String> splitExpression = ep.splitExpression(removeExtraSpaces(input));
			String rule = ep.matchRule(splitExpression);
			//System.out.println(rule);
			System.out.println(ep.parseSize(splitExpression, rule));
		}
	}
}