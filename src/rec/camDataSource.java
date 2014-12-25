package rec;

import java.io.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JOptionPane;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

public class camDataSource {
    
    private Component parent;
    DataSource ds = null;
	Player mediaPlayer = null;
    private DataSource mainCamSource;
    private MediaLocator ml;
    private Processor processor;
    private boolean processing;
    
    public camDataSource(Component parent) {
        this.parent = parent;
        setProcessing(false);
    }
    
    public void setMainSource(){
        setProcessing(false);
       /* VideoFormat vidformat = new VideoFormat(VideoFormat.YUV);
        Vector devices = CaptureDeviceManager.getDeviceList(new 
                AudioFormat("linear", 44100, 16, 2));
        CaptureDeviceInfo di = null;
        
        if (devices.size() > 0) di = (CaptureDeviceInfo) devices.elementAt(0);
        else {
           JOptionPane.showMessageDialog(parent, "Your camera is not connected", "No webcam found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for(int i=0;i<devices.size();i++)
        JOptionPane.showMessageDialog(parent, devices.get(i), "No webcam found", JOptionPane.WARNING_MESSAGE);
        try {
            ml = new MediaLocator("vfw://0");
            setMainCamSource(Manager.createDataSource(ml));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Exception locating media: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }*/
        Vector captureDevices = null;
		captureDevices = CaptureDeviceManager.getDeviceList(null);
		System.out.println("- number of capture devices: " + captureDevices.size());
		CaptureDeviceInfo video = this.getVideoDevice(captureDevices);
		CaptureDeviceInfo audio = this.getAudioDevice(captureDevices);
		
		this.ds = createDataSource(audio, video);
		setMainCamSource(ds);
    }
    
	
	public CaptureDeviceInfo getVideoDevice(Vector<CaptureDeviceInfo> captureDevices){
		for (CaptureDeviceInfo cdi : captureDevices){
			System.out.println("    - name of the capture device: " + cdi.getName());
			for(Format format: cdi.getFormats()){
				if (format instanceof VideoFormat) {
					System.out.println("         - format accepted by this VIDEO device: "
										+ format.toString().trim());
					return cdi;
				}
			}
		}
		return null;
	}

	public CaptureDeviceInfo getAudioDevice(Vector<CaptureDeviceInfo> captureDevices){
		for (CaptureDeviceInfo cdi : captureDevices){
			System.out.println("    - name of the capture device: " + cdi.getName());
			for(Format format: cdi.getFormats()){
				if (format instanceof AudioFormat) {
					System.out.println("         - format accepted by this Audio device: "
										+ format.toString().trim());
					return cdi;
				}
			}
		}
		return null;		
	}
	
	public DataSource createDataSource(CaptureDeviceInfo audio, CaptureDeviceInfo video){
		DataSource audioDS = null;
		DataSource videoDS = null;
		if (audio != null){
			try {
				audioDS = javax.media.Manager.createDataSource(audio.getLocator());
			} catch (Exception e) {
				System.out.println("-> Couldn't connect to audio capture device");
			}
		}
		if (video != null){
			try {
				videoDS = javax.media.Manager.createDataSource(video.getLocator());
			} catch (Exception e) {
				System.out.println("-> Couldn't connect to video capture device");
			}
		}
		
		if (audioDS != null && videoDS != null){
			try {
				//create the 'audio' and 'video' DataSource
				return javax.media.Manager.createMergingDataSource(
						new DataSource[] { audioDS, videoDS });
			}catch (Exception e) {
				System.out.println("-> Couldn't connect to audio or video capture device");
			}
		}else if(audioDS != null){
			return audioDS;
		}else if(videoDS != null){
			return videoDS;
		}
		return null;
	}
    public void makeDataSourceCloneable(){
        
        setMainCamSource(Manager.createCloneableDataSource(getMainCamSource())); // turn our data source to a cloneable data source
        
    }
    
    public void startProcessing(){
        
        try{
            processor = Manager.createProcessor(getMainCamSource());
        }catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "IO Exception creating processor: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }catch (NoProcessorException e) {
            JOptionPane.showMessageDialog(parent, "Exception creating processor: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        camStateHelper playhelper = new camStateHelper(processor);
        if(!playhelper.configure(10000)){
            JOptionPane.showMessageDialog(parent, "cannot configure processor", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
       
        processor.setContentDescriptor(null);
        
        if(!playhelper.realize(10000)){
            JOptionPane.showMessageDialog(parent, "cannot realize processor", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
       
        
         
        processor.start(); // In order for or your clones to start, you must start the original source
       /* try{
            Thread.sleep(10000);
        }catch(java.lang.InterruptedException e){}
        
         Control control = processor.getControl("javax.media.control.FrameRateControl");
        if ( control != null && control instanceof javax.media.control.FrameRateControl ){
           ((javax.media.control.FrameRateControl)control).setFrameRate(0.2f);
        } else{
            System.out.println("no frame control");
        }*/
        
        setProcessing(true);
    }
    
    public DataSource cloneCamSource(){
        if(!getProcessing()) setMainSource();
        return ((SourceCloneable)getMainCamSource()).createClone();
              // return processor.getDataOutput();
   }
   
   public DataSource getMainCamSource(){
       return mainCamSource;
   }
   
   public void setMainCamSource(DataSource mainCamSource){
       this.mainCamSource = mainCamSource;
   }
   
   public void setMl(MediaLocator ml){
       this.ml = ml;
   }
   
  public MediaLocator getMl(){
       return ml;
   }
   
   public boolean getProcessing(){
       return processing;
   }
   
   public void setProcessing(boolean processing){
       this.processing = processing;
       
   }
   
   public void setParent(Component parent){
       this.parent = parent;
   }
   
   public Component getParent(){
       return parent;
   }
} 
