/* Bluetooth Mate Echo
  by: Jim Lindblom - jim at sparkfun.com
  date: 3/15/11
  license: CC-SA v3.0 - Use this code however you'd like, for any
  purpose. If you happen to find it useful, or make it better, let us know!
  
  This code allows you to send any of the RN-42 commands to the
  Bluetooth Mate via the Arduino Serial monitor. Characters sent
  over USB-Serial to the Arduino are relayed to the Mate, and
  vice-versa.
  
  Here are the connections necessary:
  Bluetooth Mate-----------------Arduino
      CTS-I    (not connected)
      VCC------------------------5V or 3.3V
      GND--------------------------GND
      TX-O-------------------------D2
      RX-I-------------------------D3
      RTS-O    (not connected)
  
  How to use:
  You can use the serial monitor to send any commands listed in
  the RN-42 Advanced User Manual
  (http://www.sparkfun.com/datasheets/Wireless/Bluetooth/rn-bluetooth-um.pdf)
  to the Bluetooth Mate.
  
  Open up the serial monitor to 9600bps, and make sure the 
  pull-down menu next to the baud rate selection is initially set
  to "No line ending". Now enter the configuration command $$$ in 
  the serial monitor and click Send. The Bluetooth mate should
  respond with "CMD".
  
  The RN-42 module expects a newline character after every command.
  So, once you're in command mode, change the "No line ending"
  drop down selection to "Newline". To test, send a simple command.
  For instance, try looking for other bluetooth devices by sending
  the I command. Type I and click Send. The Bluetooth Mate should
  respond with "Inquiry, COD", follwed by any bluetooth devices
  it may have found.
  
  To exit command mode, either connect to another device, or send
  ---.
  
  The newline and no line ending selections are very important! If
  you don't get any response, make sure you've set that menu correctly.
*/

// We'll use the newsoftserial library to communicate with the Mate
#include <SoftwareSerial.h>  


int bluetoothTx = 2;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 3;  // RX-I pin of bluetooth mate, Arduino D3
int led = 13;

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{
  pinMode(led, OUTPUT);
  
  Serial.begin(9600);  // Begin the serial monitor at 9600bps
  
  bluetooth.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  bluetooth.print("$$$");  // Enter command mode
  delay(100);  // Short delay, wait for the Mate to send back CMD
  bluetooth.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  bluetooth.begin(9600);  // Start bluetooth serial at 9600
}

void loop()
{ 
  if(bluetooth.available())  // If the bluetooth sent any characters
  {
    char val = bluetooth.read();
    
    if(val)
    {
      digitalWrite(led,HIGH);
    }
    else
    {
      digitalWrite(led, LOW);
    }
    
    // Send any characters the bluetooth prints to the serial monitor
    Serial.print(val);  
  }
  if(Serial.available())  // If stuff was typed in the serial monitor
  {
    // Send any characters the Serial monitor prints to the bluetooth
    bluetooth.print((char)Serial.read());
    
  }
  // and loop forever and ever!
}
