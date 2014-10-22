import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Table {
    
    private List<WebElement> sourceTable;
    private String[][] table;
    private String[] problemNames;
    private int problemsNumber;
    private int studentsNumber;
    
    public Table(WebDriver driver, String url){
        driver.get(url);
        this.sourceTable = driver.findElements(By.xpath("/html/body/div/div[3]/table/tbody/tr[2]/td[2]/div/div/div/table/tbody/tr")); // get the table
        List<WebElement> problems = this.sourceTable.get(0).findElements(By.tagName("td")); // get problems line of table
        this.problemsNumber = problems.size()-4; // get number of problems
        
        /* get problem names */
        this.problemNames = new String[problems.size()-4];
        for(int i=2;i<problems.size()-2;i++){
            this.problemNames[i-2] = problems.get(i).getText();
        }
        this.sourceTable.remove(0); // remove problems line from table
           
        this.studentsNumber = getStudentsNumber();
        this.table = new String[this.studentsNumber][this.problemsNumber];
        
        WebElement a;
        List<WebElement> b;
        char firstChar;
        for(int i=0;i<this.studentsNumber;i++){
            a = this.sourceTable.get(i);
            b = a.findElements(By.tagName("td"));
            for(int j=0;j<this.problemsNumber;j++){
                firstChar = b.get(j+2).getText().charAt(0);
                if(firstChar == '+'){
                    this.table[i][j] = "+";
                }else{
                    this.table[i][j] = "-";
                }
            }
        }
    }
    
    public String[][] getTable(){
        return this.table;
    }
    
    private int getStudentsNumber(){
        int num = 0;
        List<WebElement> line;
        String row;
        for(WebElement a:this.sourceTable){
            line = a.findElements(By.tagName("td"));
            row = line.get(0).getText();
            try{
                num = Integer.parseInt(String.valueOf(row.charAt(row.length()-1)));
            }catch(NumberFormatException e){
                break;
            }
        }
        return num;
    }
    
    public void printProblemNames(){
        for(String s:this.problemNames){
            System.out.print(s+" ");
        }
        System.out.println();
    }
    
    public String[] getProblemNames(){
        return this.problemNames;
    }
    
    public void printTable(){
        this.printProblemNames();
        for(String[] a:this.table){
            for(String b:a){
                System.out.print(b+" ");
            }
            System.out.println();
        }
    }
    
    public void printSourceTable(){
        for(WebElement a:this.sourceTable){
            for(WebElement b:a.findElements(By.tagName("td"))){
                System.out.print(b.getText()+" ");
            }
            System.out.println();
        }
    }  
    
}