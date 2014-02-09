import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

import KBS.Math.Equals;
import KBS.Math.Greaterthan;
import KBS.Math.GreaterthanEquals;
import KBS.Math.Lessthan;
import KBS.Math.LessthanEquals;
import KBS.Math.NotEquals;
import KBS.Math.Operator;

import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;


public class LOQR {
	
	// this is the hash map which stores the methods of the abstract class.
	static Map<String, Operator> operator = new HashMap<String,Operator>();
	static {
		operator.put("=", new Equals());
		operator.put(">", new Greaterthan());
		operator.put(">=", new GreaterthanEquals());
		operator.put("<", new Lessthan());
		operator.put("<=", new LessthanEquals());
		operator.put("!=", new NotEquals());
	}
	
	
// This method takes the maincolumn - the column with which the constraints are checked,allcolumn array, the operator and the argument.
// we have opened the csv file and created a dataContext using meta model, with which we will query the csv file.
	public void ExtractDomainKnowledge(String MainColumn,String[] allColumns,String arg,String opr) throws Exception{
		
		File myFile = new File("Data/MusicDataSet.csv");

		DataContext dataContext = DataContextFactory.createCsvDataContext(myFile);
		Schema schema = dataContext.getDefaultSchema();
		Table[] tables = schema.getTables();

		// CSV files only has a single table in the default schema
		assert tables.length == 1;

		Table table = tables[0];

		// get columns by name and stores them in a arraylist as column object, need this to 
		ArrayList<Column> numberofColumns = new ArrayList<Column>();
		StringBuilder columnString = new StringBuilder();
		for(int k=0;k<allColumns.length;k++){
			numberofColumns.add(table.getColumnByName(allColumns[k]));
			
		}

		//  created a column comma seperated string for the header in the csv
		for(int j=0;j<numberofColumns.size();j++){
			if(j==numberofColumns.size()-1){
			columnString.append(numberofColumns.get(j).getName());
			}else{
				columnString.append(numberofColumns.get(j).getName());
				columnString.append(",");
			}
		}
		// constructed the query which fetched all the columns in the query - creating a projection ****
		Query q = dataContext.query().from(table).select(allColumns).toQuery();
		
		DataSet dataSet = dataContext.executeQuery(q);
		StringBuffer sb = new StringBuffer();
		// line separator to add for each iteration.
		final String separator = System.getProperty("line.separator");
		sb.append(columnString);
		sb.append(separator);
		while (dataSet.next()) {
			// getting two arguments to be validated for > or < , depending on the operator.
			double argA = Double.parseDouble((String) dataSet.getRow().getValue(table.getColumnByName(MainColumn)));
			double argB = Double.parseDouble(arg);
			// I have declared a Abstract class , the hashmap returns the class objects with which i am able to compute in runtime.
			Operator myopr = operator.get(opr);
			
			for(int j=0;j<numberofColumns.size();j++){
				if(j==numberofColumns.size()-1){
					if(MainColumn.equalsIgnoreCase(numberofColumns.get(j).getName().trim())){
						if(myopr.compute(argA, argB)){
							sb.append("YES");
						}else{
							sb.append("NO");
						}
					}else{
						sb.append(dataSet.getRow().getValue(numberofColumns.get(j)));	
					}
					
					
				}else{
					if(MainColumn.equalsIgnoreCase(numberofColumns.get(j).getName().trim())){
						if(myopr.compute(argA, argB)){
							sb.append("YES");
							sb.append(",");	
						}else{
							sb.append("NO");
							sb.append(",");	
						}
					}else{
						sb.append(dataSet.getRow().getValue(numberofColumns.get(j)));
						sb.append(",");	
					}
			
				}
				
				
			}
			sb.append(separator);
		}
		dataSet.close();
		// writing the buffer to the csv file.
		try {
				  BufferedWriter out = new BufferedWriter( new FileWriter("Temp_Files/"+MainColumn+".csv"));
				  out.write(sb.toString());
				  out.close();
				    }
				catch (IOException e)
				    {
				    e.printStackTrace();
				    }
		
		CSVLoader loader = new CSVLoader();
	    loader.setSource(new File("Temp_Files/"+MainColumn+".csv"));
	    Instances data = loader.getDataSet();
	 
	    // save the CSV to a ARFF file to use for j48 classifier.
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(new File("Temp_Files/"+MainColumn+".arff"));
	    saver.writeBatch();
		
	}
	// the method is a j48 classifier an implementation of C4.5 algorithm 
	public void C45(String [] fileName,String [] arg, String[] opr,String UserQuery) throws Exception{
		Vector parent = null;
		Vector output = new Vector();
		String tempString = null;
		String[] tempArray;
		// loop the process for all the columns  - i.e all the files should be classified to get rules.
		for(int i=0;i<fileName.length;i++){
			
			BufferedReader reader = new BufferedReader(new FileReader("Temp_Files/"+fileName[i]+".arff"));
			Instances data = new Instances(reader);
			reader.close();
			// j48 classifier object 
		J48 dec = new J48();
		data.setClassIndex(i);
		dec.buildClassifier(data);
		// part object to get the rules from the j48 classifier.
		PART pt = new PART();
		pt.buildClassifier(data);
		// I have recompiled weka jar with m_root variable as public, otherwise its impossible to get the rule vector from the 
		Vector child = pt.m_root.getRules();
		parent = new Vector();
		Enumeration inputEnum = child.elements();
		// seperated the rule from the yes/ no string
		while (inputEnum.hasMoreElements())
		{
		tempString = inputEnum.nextElement().toString();
		tempArray = tempString.split(":");
		if (!((tempArray[0].contains("YES")) || (tempArray[0].contains("NO"))))
			if(!tempArray[0].equalsIgnoreCase("")){
				String Statement = tempArray[0] +" ^ " + fileName[i] +" "+ opr[i] +" "+ arg[i];
				parent.add(Statement);
			}
		
		}
		for(int j=0;j<parent.size();j++){
			output.add(parent.get(j));	
		}
		
		}
		// check the refiner - distance to get the rle with min distance.
		RefinerStatement d = new RefinerStatement();
		int i =d.CheckDistance(output, UserQuery);
		Relax rx = new Relax();
		// refine the query as per the algorithm in the paper.
		rx.refineQuery(UserQuery, output.get(i).toString().replace("AND", " ^ "));

		
	}
	

	public static void main(String [] args) throws Exception{
		
		
		String inputFile, userInput;
		int i = 0, n;
		int counter = 0;
		String symbols[] = {"<", ">=", ">", "<="};
		
		HashMap<Integer, String> h = new HashMap<Integer, String>();
		BufferedReader filebr = new BufferedReader (new FileReader ("Data/Columns.txt"));
		
		while ((inputFile = filebr.readLine()) != null)
		{
			h.put(i, inputFile);
			i++;
		}
		
		i = 0;
		BufferedReader inputbr = new BufferedReader (new InputStreamReader (System.in));
		
		System.out.println ("Note: Example Input Format: rhythm_attack_time_PeriodEntropy > 0.123 ^ rhythm_attack_slope_Slope < 0.234");
		Thread.sleep(3000);
		System.out.println ("\n\nEnter the input conditions");
		String inp = inputbr.readLine();
		userInput = inp.replace(">=", ">").replace("<=", "<");
		
		String[] inputParts = userInput.split("\\^");
		n = inputParts.length;
		
		String QueryString[] = new String[n], op[]= new String[n], arg[]= new String[n];
		
		for(i = 0; i < inputParts.length; i++)
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
		
		
		for (i = 0; i < n; i++)
		{
			if (h.containsValue(QueryString[i]))
			{
				counter++;
			}
		}
		
		if (counter != n)
		{
			System.out.println ("\nError in Input. Check attribute names and input format. Run the program again");
			System.exit(0);
		}
		else{
			System.out.println ("\nInput Accepted");
		}
	

		
		LOQR kp = new LOQR();
		for(int k=0;k<QueryString.length;k++){
			kp.ExtractDomainKnowledge(QueryString[k],QueryString,arg[k],op[k]);
			
		}
		
		kp.C45(QueryString,arg,op,userInput);
		}

	

}
