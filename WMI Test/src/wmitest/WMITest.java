package wmitest;

import java.io.*;

import gnu.io.*;

import java.lang.Math;

public class WMITest {
	
	private static String cpuTempQuery = "wmic /NAMESPACE:\\\\root\\OpenHardwareMonitor PATH Sensor WHERE Identifier='/intelcpu/0/temperature/4' GET Value";
	private static String cpuLoadQuery = "wmic /NAMESPACE:\\\\root\\OpenHardwareMonitor PATH Sensor WHERE Identifier='/intelcpu/0/load/0' GET Value";
	private static String gpuTempQuery = "wmic /NAMESPACE:\\\\root\\OpenHardwareMonitor PATH Sensor WHERE Identifier='/nvidiagpu/0/temperature/0' GET Value";
	private static String gpuLoadQuery = "wmic /NAMESPACE:\\\\root\\OpenHardwareMonitor PATH Sensor WHERE Identifier='/nvidiagpu/0/load/0' GET Value";
	
	private static String fullQuery = "wmic /NAMESPACE:\\\\root\\OpenHardwareMonitor PATH Sensor WHERE " + 
										"\"Identifier='/intelcpu/0/temperature/4' or Identifier='/intelcpu/0/load/0'" +
										"or Identifier='/nvidiagpu/0/temperature/0' or Identifier='/nvidiagpu/0/load/0'\" GET Value";

	public static void main(String[] args) throws IOException, InterruptedException {
		
		listPorts();
		//String valueString = getSensorData(fullQuery);
		//System.out.println("And: ");
	//	valueString = valueString + '\n';
	//	System.out.println(valueString);
		
		System.out.println("Starting");
		

      
       
        try {
            (new WMITest()).connect("COM3");
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		

	}
	
	/*
Name         SensorType   Value     
GPU Core     Temperature  31        
CPU Package  Temperature  42        
GPU Core     Load         0         
CPU Total    Load         6.640625  
	 */
	public static String getSensorData(String wmicQuery) throws IOException, InterruptedException {
		String retString = "";
		Process proc = Runtime.getRuntime().exec(fullQuery); //rt.exec(commands);
		
		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new 
		     InputStreamReader(proc.getErrorStream()));

		// read the output from the command
		System.out.println("Reply:");
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			if (!s.isEmpty()) {
				if (!s.trim().equals("Value")) {
					//System.out.println(s);
					Integer val = Math.round(Float.parseFloat(s.trim()));
					retString = retString + " " + val;
				}
			}
		}
		proc.waitFor();

		// read any errors from the attempted command

		while ((s = stdError.readLine()) != null) {
			System.out.println("Errors (if any):\n");
		    System.out.println(s);
		}
		proc.waitFor();
		
		stdInput.close();
		stdError.close();
		
		return retString.trim();
	}

    public static void listPorts()
    {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()) );
        }        
    }
    
    public static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }
    
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                (new Thread(new SerialReader(in))).start();
                (new Thread(new SerialWriter(out))).start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    /** */
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                    System.out.print(new String(buffer,0,len));
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }

    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                int c = 0;
                	while (true) {
						String valueString = getSensorData(fullQuery);
						
						System.out.println(valueString);
						
						valueString = valueString + '\n';
						byte[] byteArray = valueString.getBytes();
						
						for (int i=0;i<byteArray.length;i++) {
							this.out.write(byteArray[i]);
						}
						Thread.sleep(1000);
                	}

                
               // while ( ( c = System.in.read()) > -1 )
               // {
                //    this.out.write(c);
               // }                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
        }
    }


}
