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

	private List<String> splitExpression(String expression)
	{
		return Arrays.asList(expression.split(" "));
	}

	private boolean matchRule(List<String> input)
	{

		boolean checkedSize = false;
		boolean result = false;

		String inputElement = "";
		String ruleElement = "";

		loop:
		for(int i=0; i< lookupTable.size(); i++)
		{
			checkedSize = false;
			List<String> rule = lookupTable.get(keywords.get(i));
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
				}

				else if(ruleElement.equals("starter"))
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
			}
			
			result = true;
			break loop;
		}

		return result;
	}

	private static String removeExtraSpaces(String sizeExpression)
	{
		/*boolean isConsecutive = false;
		char c = '\0';
		StringBuilder sb = new StringBuilder(); 

		String temp = sizeExpression;
		temp = temp.trim();

		for(int i=0; i< temp.length(); i++)
		{
			c = temp.charAt(i);
			if(Character.isWhitespace(c))
			{
				if(!isConsecutive)
				{
					isConsecutive = true;
					sb.append(c);
				}
				else
				{
					continue;
				}	
			}

			else
			{
				isConsecutive = false;
				sb.append(c);
			}
		}
				return sb.toString();

		*/

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

	private static String isolateRange(String range)
	{
		boolean reachedRange = false;
		StringBuilder sb = new StringBuilder();
		String s = "";

		for(int i=0; i< range.length(); i++)
		{
			if(isNumeric(range.substring(i, i+1)))
			{
				reachedRange = true;

				if(reachedRange)
				{
					sb.append(range.charAt(i));
				}
			}
		}
		
		return sb.toString();
	}

	private static List<Double> isolateNumerals(String[] arr)
	{
		List<Double> numerals = new ArrayList<Double>();

		String [] rangeSplit = new String[2];
		//rangeSplit[0] = "-1.0";
		//rangeSplit[1] = "-1.0";

		StringBuilder sb = new StringBuilder(); //"Equal to 1-9"
		String tmp = "";
		String s = "";

		try
		{
			for(int i=0; i< arr.length; i++)
			{
				tmp = arr[i];
				rangeSplit = tmp.split("-", 2);

				if((rangeSplit.length > 1))
				{
					rangeSplit[0] = isolateRange(rangeSplit[0]);
					rangeSplit[1] = isolateRange(rangeSplit[1]);

					double lowerBound = Double.parseDouble(rangeSplit[0]);
					double upperBound = Double.parseDouble(rangeSplit[1]);

					while(upperBound > lowerBound)
					{
						numerals.add(lowerBound);
						lowerBound += 0.5;
					}
					continue;
				}
				else
				{
					for(int j=0; j< tmp.length(); j++)
					{
						if(isNumeric(s = tmp.substring(j, j+1)))
						{
							sb.append(s);
						}
					}
					numerals.add(Double.parseDouble(sb.toString()));
					sb.setLength(0);
				}
			}
		}
		catch(NumberFormatException nfe)
		{
			return null;
		}

		return numerals;	
	}

	public static List<Double> parseNumerals(String newExpression)
	{
		double temp = 0.0;
		boolean reachedDigit = false;
		String s = "";
		StringBuilder numeralBuilder = new StringBuilder();   //Equal to 1-9
		List<Double> sizes = new ArrayList<Double>();

		String[] elements = newExpression.split(",", MAXSIZES);

		if((elements.length != 1) || ((elements[0].contains("-")) && (elements[0].contains("equals"))))
	    {
			sizes = isolateNumerals(elements);
		}
		
		else
		{
			try
			{
				for(int i=0; i< newExpression.length(); i++)
				{
					if((reachedDigit) && (!(s = newExpression.substring(i, i + 1)).equals(" ")))
					{
						numeralBuilder.append(s);

						if((i == newExpression.length() - 1) && (numeralBuilder.length() > 0))
						{
							temp = Double.parseDouble(numeralBuilder.toString());

							if(!isValidSize(Double.toString(temp)))
							{
								return null;
							}
							sizes.add(temp);
						}
					}

					else if(!isNumeric(s = newExpression.substring(i, i+1)))
					{
						if(reachedDigit)
						{
							reachedDigit = false;
							temp = Double.parseDouble(numeralBuilder.toString());

							if(!isValidSize(Double.toString(temp)))
							{
								return null;
							}
							sizes.add(temp);
							numeralBuilder.setLength(0);
						}
					}

					else if(isNumeric(s = newExpression.substring(i, i+1)))
					{
						reachedDigit = true;
						numeralBuilder.append(s);

						if((i == newExpression.length() - 1) && (numeralBuilder.length() > 0))
						{
							temp = Double.parseDouble(numeralBuilder.toString());

							if(!isValidSize(Double.toString(temp)))
							{
								return null;
							}
							sizes.add(temp);
						}
					}
				}
			}
			catch(NumberFormatException nfe)
			{
				return null;
			}
		}

		if(sizes == null)
		{
			return null;
		}

		Collections.sort(sizes);
		return sizes;
	}

	private static boolean isValidSize(String sizeStr) {

		double size = Double.parseDouble(sizeStr);
		boolean result = true;

		
		if(sizeStr.length() > 4)
		{
			result = false;
		}
		return result;

	}

	public static boolean isNumeric(String str) { 
	  try {  
	    Double.parseDouble(str);  
	    return true;
	  } catch(NumberFormatException e){  
	    return false;  
	  }  
	}

	public static String parseSize(String newExpression)
	{
		newExpression = removeExtraSpaces(newExpression);
		newExpression = newExpression.toLowerCase();

		int randomIndex = newExpression.indexOf("any");

		if(randomIndex != -1)
		{
			return "(size == size)";
		}
		
		double lowerBound = 0;
		double upperBound = 0;
		int conditions = 0;
		List<Double> sizes = parseNumerals(newExpression);   

		if(sizes == null)
		{
			System.err.print("Sizes were entered in incorrectly. Please re-enter a proper size/size range.");
			return null;
		}

		if(sizes.size() == 0)
		{
			System.err.println("Sizes were not properly formatted... Please re-enter.");
			return null;
		}
		
		String sign1 = "";
		String sign2 = "";

		StringBuilder generatedExpression = new StringBuilder();

		int betweenIndex = newExpression.indexOf("between");
		int greaterThanEqualIndex = newExpression.indexOf("greater than or equal to");
		int lessThanEqualIndex = newExpression.indexOf("less than or equal to");
		int greaterThanIndex = newExpression.indexOf("greater than");
		int lessThanIndex = newExpression.indexOf("less than");
		int equalsIndex = newExpression.indexOf("equals");

		if(betweenIndex != -1)
		{
			conditions = 2;
			lowerBound = sizes.get(0);
			upperBound = sizes.get(1);
			sign1 = ">";
			sign2 = "<";
		}

		else if(greaterThanIndex != -1)
		{
			conditions = 1;
			lowerBound = sizes.get(0);
			sign1 = ">";
		}

		else if(greaterThanEqualIndex != -1)
		{
			conditions = 1;
			lowerBound = sizes.get(0);
			sign1 = ">=";
		}

		else if(lessThanIndex != -1)
		{
			conditions = 2;
			upperBound = sizes.get(0);
			lowerBound = 0;
			sign1 = ">";
			sign2 = "<";
		}

		else if(lessThanEqualIndex != -1)
		{
			conditions = 2;
			upperBound = sizes.get(0);
			lowerBound = 0;
			sign1 = ">=";
			sign2 = "<=";

		}

		else if(equalsIndex != -1)
		{
			conditions = sizes.size();
			lowerBound = sizes.get(0);
			sign1 = "==";
		}

		else
		{
			System.err.println("Expression was not properly formatted... Please re-enter by using the examples.");
			return null;
		}

		for(int j=1; j<= conditions; j++)
		{
			if(j == 1)
			{
				generatedExpression.append("(size");   
				generatedExpression.append(" " + sign1 + " ");
				generatedExpression.append(lowerBound + ")" + " ");
			}

			if(j > 1)
			{
				if(equalsIndex != -1)
				{
					generatedExpression.append(" || ");
					generatedExpression.append("(size");
					generatedExpression.append(" " + sign1 + " ");
					generatedExpression.append(sizes.get(j-1) + ")");
				}
				else
				{
					generatedExpression.append("&& ");
					generatedExpression.append("(size");
					generatedExpression.append(" " + sign2 + " ");
					generatedExpression.append(upperBound + ")");
				}
			}
		}

		return generatedExpression.toString();
	}

	public static void main(String[] args)
	{
		
		ExpressionParser ep = new ExpressionParser();

		ep.fillKeywords();
		ep.fillBetweenRule();

		Scanner sc = new Scanner(System.in);
		String input = "";
		assert parseSize("BETWEEN 19.0          and            19.0").equals("(size > 7.0) && (size < 19.0)");
		assert parseSize("gReAtEr tHaN oR eQuAl To 10.5").equals("(size >= 10.5)");
		assert parseSize("less than    or  equal to   6.5").equals("(size > 0.0) && (size <= 6.5)");
		assert parseSize("GREAter                            than 12.5             ").equals("(size > 12.5)");
		assert parseSize("Equals 5.0").equals("(size == 5.0)");
		assert parseSize(" random ").equals("(size == size)");
		assert parseSize("Between a and b").equals(null);

		while(true)
		{
			System.out.println("<<<Please enter in a range of sizes.");
			input = sc.nextLine();
			System.out.println();
			//System.out.println(ep.splitExpression(removeExtraSpaces(input)));
			System.out.println(ep.matchRule(ep.splitExpression(removeExtraSpaces(input))));
		}
	}
}