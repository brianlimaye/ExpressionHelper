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

	private boolean hasRange = false;

	private Map<String, List<String>> lookupTable = new HashMap<String, List<String>>();


	private List<String> keywords = new ArrayList<String>();

	private List<String> betweenRule = new ArrayList<String>();
	private List<String> greaterRule = new ArrayList<String>();
	private List<String> greaterEqualRule = new ArrayList<String>();
	private List<String> lessRule = new ArrayList<String>();
	private List<String> lessEqualRule = new ArrayList<String>();
	private List<String> equalRule = new ArrayList<String>();

	private List<String> equalsDigits = new ArrayList<String>();

	private String currentKeyword = "";
	private List<String> secondaryExpressions = new ArrayList<String>();

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

	private boolean assertLastDigit(List<String> expression)
	{
		String element = "";

		if(!currentKeyword.equals("equals"))
		{
			return false;
		}

		for(int i=0; i< expression.size(); i++)
		{
			element = expression.get(i).trim();
			if(i != expression.size() - 1)
			{
				if(isNumeric(element))
				{
					return false;
				}
			}
			else
			{
				if(!isNumeric(element))
				{
					return false;
				}
			}
		}

		return true;
	}

	private List<String> getEqualsDigits(List<String> expression)
	{
		if(expression == null)
		{
			return null;
		}

		double lowerBound = 0;
		double upperBound = 0;

		String element = "";
		boolean containsNumerals = false;
		List<String> digits = new ArrayList<String>();

		List<String> temp = new ArrayList<String>();
		int len = expression.size();

		loop:
		for(int i=0; i< expression.size(); i++)
		{
			element = expression.get(i).trim();

			String[] parts = element.split(",", 25);

			if(parts.length > 1)
			{
				for(int j=0; j< parts.length; j++)
				{
					if(parts[j].contains("-"))
					{
						String[] range = parts[j].split("-", 2);

						if(range.length == 2)
						{
							for(int k=0; k< range.length; k++)
							{
								if(!isNumeric(range[k]))
								{
									continue loop;
								}

								if(k == 0)
								{
									lowerBound = Double.parseDouble(range[k]);
								}
								else
								{
									upperBound = Double.parseDouble(range[k]);
								}
							}

							if((lowerBound != 0) && (upperBound != 0))
							{
								secondaryExpressions.add(new String("Size is between " + lowerBound + " and " + upperBound));

								if(j == parts.length - 1)
								{
									return digits;
								}
								continue loop;
 							}
						}
					}

					if(!isNumeric(parts[j]))
					{
						continue loop;
					}
					digits.add(parts[j]);
				}
				return digits;
			}
		}

		if(expression.get(len - 1).contains("-"))
		{
			String[] range = expression.get(len - 1).split("-", 2);

			if(range.length > 1)
			{
				if((isNumeric(range[0])) && (isNumeric(range[1])))
				{
					lowerBound = Double.parseDouble(range[0]);
					upperBound = Double.parseDouble(range[1]);
					secondaryExpressions.add(new String("Size is between " + lowerBound + " and " + upperBound));
					this.hasRange = true;
					return temp;
				}
			}
		}

		if(assertLastDigit(expression))
		{
			temp.add(expression.get(len - 1));
			return temp;
		}

		return null;
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
			currentKeyword = keyword;

			/*
			if(input.size() != lookupTable.get(keyword).size())
			{
				return null;
			}
			*/

		
			if(input.size() < 3)
			{
				continue;
			}

			/*
			if((currentKeyword.equals("equals")) && (getEqualsDigits(input) == null))
			{
				continue;
			}
			*/

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
		if((input.size() != rule.size()) && (!currentKeyword.equals("equals")))
		{
			return false;
		}

		if((currentKeyword.equals("equals")) && (input.size() > rule.size()))
		{
			return false;
		}

		List<Double> digits = new ArrayList<Double>();
		List<Double> sortedDigits = new ArrayList<Double>();
		List<String> equalsSizes = new ArrayList<String>();

		int count = 0;

		boolean checkedSize = false;
		boolean result = false;

		String inputElement = "";
		String ruleElement = "";

		if(currentKeyword.equals("equals"))
		{
			equalsSizes = getEqualsDigits(input);
		}

		loop:
		for(int j=0; j< rule.size(); j++)
		{
			if((!checkedSize) && (input.size() != rule.size()) && (!currentKeyword.equals("equals")))
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
				if(currentKeyword.equals("equals"))
				{
					if(equalsSizes != null)
					{
						count++;
						continue loop;
					}
				}

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

	    if(currentKeyword.equals("equals"))
	    {
	    	if(equalsSizes != null)
	    	{
	    		this.equalsDigits = equalsSizes;
	    		return true;
	    	}
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

		StringBuilder sb = new StringBuilder();

		if(!hasRange)
		{
			sb.append(buildExpression(list, rule));
		}

		for(int i=0; i< secondaryExpressions.size(); i++)
		{

			if(!hasRange)
			{
				sb.append("|| ");
			}

			List<String> secondary = splitExpression(removeExtraSpaces(secondaryExpressions.get(i)));
			sb.append(buildExpression(secondary, "between"));
		}

		hasRange = false;
		equalsDigits.clear();
		secondaryExpressions.clear();
		currentKeyword = "";

		return sb.toString();
	}

	private String buildExpression(List<String> list, String rule)
	{
		if(!lookupTable.containsKey(rule))
		{
			return null;
		}

		String element = "";
		StringBuilder expressionBuilder = new StringBuilder();
		String sign1 = "";
		String sign2 = "";

		if(rule.contains("less"))
		{
			expressionBuilder.append("(");
			expressionBuilder.append("size > 0.0) && (");
		}

		if(rule.equals("equals"))
		{
			for(int i=0; i< equalsDigits.size(); i++)
			{
				expressionBuilder.append("(");
				expressionBuilder.append("size == " + Double.parseDouble(equalsDigits.get(i)) + ") ");

				if(i != equalsDigits.size() - 1)
				{
					expressionBuilder.append("|| ");
				}
			}

			return expressionBuilder.toString();
		}

		for(int i=0; i< list.size(); i++)
		{
			element = list.get(i);
			element = element.toLowerCase();

			switch(element)
			{
				case "size":
					expressionBuilder.append("(");
					expressionBuilder.append(element + " ");
					break;
				case "style":
					expressionBuilder.append("(");
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
					sign1 = ">";
					if(!list.contains("equal"))
					{
						expressionBuilder.append(sign1 + " ");
					}
					break;
				case "less":
					sign1 = "<";
					if(!list.contains("equal"))
					{
						expressionBuilder.append(sign1 + " ");
					}
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
			//System.out.println(splitExpression);
			String rule = ep.matchRule(splitExpression);
			System.out.println(ep.parseSize(splitExpression, rule));

		}
	}
}