import java.util.*;
import java.io.*;

public class RefinerStatement
{
	
	// method gets the rules array and the inputquery as a input and find the minimum disance of each rule and ruturns the 
	//index of the least distance rule.
	public static int CheckDistance(Vector v, String inputString) throws IOException, FileNotFoundException
	{
		String symbols[] = {"<", ">=", ">", "!=", "<="}, s1;

		
		String[] inputParts = inputString.split("\\^");
		String QueryString[] = new String[inputParts.length];
		String op[]= new String[inputParts.length];
		String arg[]= new String[inputParts.length];
		
		for(int i = 0; i < inputParts.length; i++)
		{
			for(int j = 0; j < symbols.length; j++)
			{
				if (inputParts[i].contains(symbols[j]))
				{			
					String[] test = inputParts[i].split(symbols[j]);
					QueryString[i] = test[0].trim();
					op[i] = symbols[j].trim();
					arg[i] = test[1].trim();
				}		
			}			
		}
		
		double[] distArray = new double[v.size()];
		String tempString;
		double tempVal;
		String nextLine;
		double distance;
		double min = 9999;
		int minIndex = 0;
		int index = -1;
		int i = 0;
		// getting the column names from a txt and storing in a hashmap
		HashMap<Integer, String> columns = new HashMap();
		BufferedReader colbr = new BufferedReader (new FileReader ("Data/Columns.txt"));
		while ((nextLine = colbr.readLine()) != null)
		{
			columns.put(i, nextLine);
			i++;
		}
		
		i = 0;
		//getting the max values of each column and storing them in a hashmap
		HashMap<String, Double> maximum = new HashMap();
		BufferedReader maxbr = new BufferedReader (new FileReader ("Data/Maximum.txt"));
		while ((nextLine = maxbr.readLine()) != null)
		{
			maximum.put(columns.get(i), Double.parseDouble(nextLine));
			i++;
		}
		
		i = 0;
		// getting the min values of each column stored in a file to a hashmap.
		HashMap<String, Double> minimum = new HashMap();
		BufferedReader minbr = new BufferedReader (new FileReader ("Data/Minimum.txt"));
		while ((nextLine = minbr.readLine()) != null)
		{
			minimum.put(columns.get(i), Double.parseDouble(nextLine));
			i++;
		}
		
		
		i = 0;
		Enumeration<String> ven = v.elements();
		
		while (ven.hasMoreElements())
		{
			index++;
			s1 = ven.nextElement().toString().replace("AND","^");
			String[] parts = s1.split("\\^");
			String array0[] = new String[parts.length], array1[]= new String[parts.length], array2[]= new String[parts.length];
			
			
					
			for(i = 0; i < parts.length; i++)
			{
				for(int j = 0; j < symbols.length; j++)
				{
					if(parts[i].contains(symbols[j]))
					{
						String[] test = parts[i].split(symbols[j]);
						array0[i] = test[0];	
						array1[i] = symbols[j];
						array2[i] = test[1];
					}		
				}	
			}
			
			
			for (i = 0; i < QueryString.length; i++)
			{
				tempString = QueryString[i];
				for (int j = 0; j < array0.length; j++)
				{
					if (array0[j].trim().equalsIgnoreCase(tempString))
					{
						tempVal = Double.parseDouble(array2[j]);
						distance = (Math.abs(Double.parseDouble(arg[i]) - tempVal)) / (maximum.get(tempString) - minimum.get(tempString));
						distArray[index] = distArray[index] + distance;
					}
				}
			}
		}
		
		for (i = 0; i < distArray.length; i++)
		{
			if ((distArray[i] < min))
			{
				min = distArray[i];
				minIndex = i;
			}
		}
		System.out.println ("\nIndex with Minimum Value = " + minIndex);
		return minIndex;
	}
}