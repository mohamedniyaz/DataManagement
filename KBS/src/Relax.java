import java.io.File;
import java.util.ArrayList;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;


public class Relax {

	/**
	 * Relax query - refine the query .
	 */
	public static void refineQuery(String s1,String s2) {
		String temp = "";
		String s22= s2.replace("<=", "<").replace(">= ", ">");
		// Splitting the query and the resultant rule in to parts for manuplation.
		String[] parts1 = s1.split("\\^");
		String[] parts2 = s22.split("\\^");
		String s5[] ={"<",">=",">","!=","<="};
		String m_column[] = new String[parts1.length],m_opr[]= new String[parts1.length],m_value[]= new String[parts1.length];

		for(int i=0;i<parts1.length;i++)
		{
			for(int j=0;j<s5.length;j++)
			{
				if(parts1[i].contains(s5[j]))
				{

					String[] test =parts1[i].split(s5[j]);
					m_column[i]=test[0];
					m_opr[i]=s5[j];
					m_value[i]= test[1];
				}
			}

		}
		String r_column[] = new String[parts2.length],r_opr[]= new String[parts2.length],r_value[]= new String[parts2.length];

		for(int i=0;i<parts2.length;i++)
		{
			for(int j=0;j<s5.length;j++)
			{
				if(parts2[i].contains(s5[j]))
				{

					String[] test =parts2[i].split(s5[j]);
					r_column[i]=test[0];
					r_opr[i]=s5[j];
					r_value[i]= test[1];

				}
			}


		}


/**If the two constraints are of same type inequality, then we choose the least constraining of both constructs
** If the two constraints are of different inequalities, then we choose the intersection of the two constraints using checkCSV(). 
*If the result of intersection is empty, then there will be no changes to the relaxed conjunction
**/
		for(int i=0; i<m_column.length;i++){
			for(int j=0;j<r_column.length;j++){
				if(m_column[i].trim().equals(r_column[j].trim())){
					if(m_opr[i].matches("<") || m_opr[i].matches(">")){
						if(r_opr[j].matches("<") || r_opr[j].matches(">")){
							if(r_opr[j].equalsIgnoreCase(m_opr[i]))
								r_value[j]= greater(m_value[i],r_value[j]);

							else
								if((m_opr[i].matches("<") || m_opr[i].matches(">")) && (r_opr[j].matches(">") ||r_opr[j].matches("<") )){
									String temp_conj = r_column[j].trim()+" "+ r_opr[j].trim()+" "+ r_value[j].trim() + " ^ " + m_column[i].trim() +" "+ m_opr[i].trim() +" "+ m_value[i].trim() ;
									if(CheckNullSet(temp_conj)){
									r_value[j]=greater(m_value[i],r_value[j]);
									r_opr[j] = 	"<";
									temp = "^ "+ r_column[j] + ">"+ lesser(m_value[i],r_value[j]) ;
									}else{
										r_column[j]="";
										r_opr[j]="";
										r_value[j]="";
									}
								}	
						}

					}
				}
			}
		}
		String s3 = "";
		for(int i=0; i<r_column.length;i++){
			if(i==r_column.length-1){
				s3= s3 + r_column[i] + r_opr[i] + r_value[i] ;	
			}else{
			if(!r_column[i].equalsIgnoreCase("")){
				s3= s3 + r_column[i] + r_opr[i] + r_value[i] + " ^";
			}
			}
			
		}
		s3 = s3 + temp;
		String RelaxedQuery = s3.replace("^ ^", "^");
		System.out.println("**************************************************************");
		System.out.println("Relaxed Query :"+ RelaxedQuery);
		PrintRules(RelaxedQuery);

	}
// check if the two values are lesser.
	private static String lesser(String array2, String array22) {
		
		if(Double.parseDouble(array2)<Double.parseDouble(array22))
			return array2;
		else
			return array22;
	}
// Check if the two values are greater.
	private static String greater(String array2, String array22) {
			
		if(Double.parseDouble(array2)>Double.parseDouble(array22))
			return array2;
		else
			return array22;


	}
	public static boolean CheckNullSet(String s3){
		
		
		String[] parts = s3.split("\\^");
		File myFile = new File("Data/MusicDataSet.csv");
		DataContext dataContext = DataContextFactory.createCsvDataContext(myFile);
		Schema schema = dataContext.getDefaultSchema();
		Table[] tables = schema.getTables();

		// CSV files only has a single table in the default schema
		assert tables.length == 1;

		Table table = tables[0];

		// get columns by name 
		ArrayList<Column> numberofColumns = new ArrayList<Column>();
		StringBuilder columnString = new StringBuilder();
		String allColumns="MIRtoolbox1.3.4";
		
			numberofColumns.add(table.getColumnByName(allColumns));
			

	Query qw = new Query();
	qw.from(table); 
	qw.select(allColumns);

	for(int k=0;k<parts.length;k++){
	qw.where(parts[k]);	
	}
		
		DataSet dataSet = dataContext.executeQuery(qw);
		if(dataSet.next()){
			return true;
			}else{
				return false;
				
			}
		}
	// this method take the final query and queries the csv to print the output.
public static void PrintRules(String s3){
		
		
		String[] parts = s3.split("\\^");
		File myFile = new File("Data/MusicDataSet.csv");
		DataContext dataContext = DataContextFactory.createCsvDataContext(myFile);
		Schema schema = dataContext.getDefaultSchema();
		Table[] tables = schema.getTables();

		// CSV files only has a single table in the default schema
		assert tables.length == 1;

		Table table = tables[0];

		// get columns by name 
		ArrayList<Column> numberofColumns = new ArrayList<Column>();
		StringBuilder columnString = new StringBuilder();
		String allColumns="MIRtoolbox1.3.4";
		
			numberofColumns.add(table.getColumnByName(allColumns));
			

	Query qw = new Query();
	qw.from(table); 
	qw.select(allColumns);

	for(int k=0;k<parts.length;k++){
	qw.where(parts[k]);	
	}
	DataSet dataSet = dataContext.executeQuery(qw);	
	System.out.println("\n");
	System.out.println("**********************************");
	System.out.println("MIRtoolbox1.3.4");
	while (dataSet.next()) {
		 Row row = dataSet.getRow();
		System.out.println(dataSet.getRow().getValue(table.getColumnByName("MIRtoolbox1.3.4")));
		
	}

}
}
