package rec;


 public class jmfexample {
      
     
      public jmfexample() {
          
         camDataSource dataSource = new camDataSource(null);
         dataSource.setMainSource();
         dataSource.makeDataSourceCloneable();
         dataSource.startProcessing();
        mainFrame frame = new mainFrame(dataSource);
         frame.setSize(1280, 720);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
         
     }
     
     
     public static void main(String[] args) {
         
         jmfexample jmf = new jmfexample();
         
     }
     
 }