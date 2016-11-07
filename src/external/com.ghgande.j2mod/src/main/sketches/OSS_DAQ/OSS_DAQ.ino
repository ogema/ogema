/*
 * Copyright 2014, Julianne Frances Haugh
 * d/b/a greenHouse Gas and Electric
 *
 * This work is licensed under the terms of the "Creative Commons
 * Attribution-ShareAlike 4.0 International" open source license.
 * For more information, please read the following web page:
 *
 * http://creativecommons.org/licenses/by-sa/4.0/
 */
 
#include <EEPROM.h>
#include <alloca.h>
#include <avr/wdt.h>
#include <avg/pgmspace.h>

/*
 * Hardware identification.
 *
 * NOTE: If you are using this sketch in your own product you MUST replace
 *    all references to greenHouse Gas and Electric (except the copyright
 *    notice and the PROGMEN declaration below) with your own project
 *    related names.  You may not use either the name "greenHouse Gas and
 *    Electric" or "OSS_DAQ" in your product.  However, you MUST attribute
 *    your product to greenHouse Gas and Electric (CC-BY-SA).
 */
 
char copyright_notice[] PROGMEM = "Copyright 2014, greenHouse Gas and Electric, CC-BY-SA 4.0 Intl.";

/*
 * Pin assignments.
 *
 * AN0 - Interrupt 1, digital input counter #1, falling edge
 * AN1 - Interrupt 2, digital input counter #2, falling edge
 * A0 -- Analog 0, analog input sensor #1, default softare pull-up
 * A1 -- Analog 1, analog input sensor #2, default software pull-up
 * A2 -- Analog 2, analog input sensor #3, default float
 * A3 -- Analog 3, analog input sensor #4, default float
 * A4 -- Analog 4, unused, pull-up
 * A5 -- Analog 5, zener voltage reference, pull-down to GND or add voltage reference.
 */

#define HARD_COUNT_0_PIN  2
#define HARD_COUNT_1_PIN  3
#define DIGITAL_IN_2      4
#define DIGITAL_IN_3      5

#define COIL_1_PIN  6    
#define COIL_2_PIN  7
#define COIL_3_PIN  8
#define COIL_4_PIN  9

#define LED_PIN     13

unsigned    long    last_millis = 0;
unsigned    long    last_led_flip = 0;
unsigned    char    last_led_state = 0;
unsigned    long    last_sample = 0;

unsigned    char    modbus_unit = 1;
unsigned    char    modbus_tmo = 4;
unsigned    short   modbus_baud = 19200;

unsigned    char    coil_1_state = 0;
unsigned    char    coil_2_state = 0;
unsigned    char    coil_3_state = 0;
unsigned    char    coil_4_state = 0;

unsigned    char    pullups[4];

unsigned    short   device_serial = 0;

#define REG_A0 0
#define REG_A1 1
#define REG_A2 2
#define REG_A3 3
#define REG_VREF 4
#define REG_D0 5
#define REG_D1 6
#define REG_D2 7
#define REG_D3 8
#define REG_D0_CNT 9
#define REG_D1_CNT 10
#define REG_LOOP_HI 11
#define REG_LOOP_LO 12
#define REG_BAUD 13
#define REG_UNIT 14
#define REG_SERIAL 15
#define MODBUS_REGS 16

unsigned    short   modbus_regs[MODBUS_REGS];

#define MODBUS_COILS 12

#if defined(__AVR_ATmega88__) || defined(__AVR_ATmega168__)
#define  KB_RAM  1
#elif defined(__AVR_ATmega328__) || defined(__AVR_ATmega328P__)
#define  KB_RAM  2
#define  EEPROM_SIZE 1024
#elif defined(__AVR_ATmega2560__)
#define  KB_RAM  8
#define  EEPROM_SIZE 4096
#else
#error Unknown processor
#endif

/*
 * User-selectable features.
 *
 * The #define statements below may by commented out to exclude the specified
 * features.
 */
 
#define HAS_MULTI /* WRITE MULTIPLE REGISTERS / COILS */
#define HAS_SRVR_ID /* READ SERVER ID */
#define HAS_MEI /* READ MODBUS ENCAPSULATED INFO */
#define HAS_FIFO /* READ FIFO */
#define HAS_FILES /* READ/WRITE FILE RECORD */
#define HAS_COUNTS /* GET COMM EVENT COUNTER */
#define HAS_LOG /* GET COMM EVENT LOG */

#if defined(HAS_LOG) && !defined(HAS_COUNTS)
#error Event log depends on event counters.
#endif

/*
 * Values for READ DEVICE IDENTICATION method.
 */
#if defined(HAS_MEI) || defined(HAS_SRVR_ID)
#ifdef HAS_MEI
char company_name[] PROGMEM = "greenHouse Gas and Electric";
#endif
char product_name[] PROGMEM = "OSS_DAQ";
#ifdef HAS_MEI
char version_text[] PROGMEM = "v1.01";
#endif
#endif

#ifdef HAS_FIFO
/**
 * get_fifo_count - Return "31" for valid ADC queues, -1 otherwise.
 */
char get_fifo_count(char fifo) {
  if (fifo < 0 || fifo > 3)
    return -1;
    
  return 31;
}

/**
 * get_fifo_value - Return the current ADC value
 */
short get_fifo_value(char fifo) {
  switch (fifo) {
    case 0: return analogRead(A0);
    case 1: return analogRead(A1);
    case 2: return analogRead(A2);
    case 3: return analogRead(A3);
    default: return -1;
  }
}
#endif

#ifdef HAS_FILES
#define  RECORD_SIZE  64
#define  FILE_BASE 128

const int eeprom_file_base = FILE_BASE;
const int eeprom_record_count = ((EEPROM_SIZE - FILE_BASE) / (RECORD_SIZE * 2));

char file_check_entry(unsigned short file, unsigned short record, unsigned short count) {
  if (file > 0 || record >= eeprom_record_count || count > RECORD_SIZE)
    return 0;
  
  return 1;
}

unsigned short file_get_value(unsigned short file, unsigned short record, unsigned char offset) {
  int address = (eeprom_file_base +
      record * RECORD_SIZE * sizeof(unsigned short)) +
      (offset * sizeof(unsigned short));
  
  return read_eeprom_short(address);
}

void file_put_value(unsigned short file, unsigned short record, unsigned char offset, unsigned short value) {
  int address = (eeprom_file_base +
      record * RECORD_SIZE * sizeof(unsigned short)) +
      (offset * sizeof(unsigned short));

  write_eeprom_short(address, value);
}
#endif

/*
 * get_coil - Return the state of the named coil.
 */
char get_coil(char coil) {
  switch(coil) {
    case 0: return modbus_regs[REG_D0] != 0;
    case 1: return modbus_regs[REG_D1] != 0;
    case 2: return coil_1_state;
    case 3: return coil_2_state;
    case 4: return coil_3_state;
    case 5: return coil_4_state;
    case 6:
    case 7:
    case 8:
    case 9:
      return pullups[coil - 6];
    default:
      return -1;
  }
}

char is_coil_setable(unsigned char coil) {
  return (coil >= 2 && coil <= 9);
}

char set_coil(char coil, char value) {
  switch(coil) {
    case 2:
      digitalWrite(COIL_1_PIN, value ? HIGH:LOW);
      coil_1_state = value != 0;
      return 1;
    case 3:
      digitalWrite(COIL_2_PIN, value ? HIGH:LOW);
      coil_2_state = value != 0;
      return 1;
    case 4:
      digitalWrite(COIL_3_PIN, value ? HIGH:LOW);
      coil_3_state = value != 0;
      return 1;
    case 5:
      digitalWrite(COIL_4_PIN, value ? HIGH:LOW);
      coil_4_state = value != 0;
      return 1;
    case 6:
    case 7:
    case 8:
    case 9:
      pinMode(A0 + coil - 4, value ? INPUT_PULLUP:INPUT);
      pullups[coil - 6] = value != 0;
      return 1;
    default:
      return 0;
  }
}

/*
 * Hardware interrupt counters.
 */
unsigned    short   count_1;
unsigned    short   count_2;

/*
 * These EEPROM functions support reading and writing the Modbus values used by
 * the greenSensors platform.  I used the same EEPROM address here so I can test
 * on existing hardware.
 */
short read_eeprom_short(int address) {
  short result = 0;
  
  result = (((unsigned char) EEPROM.read(address)) << 8) | ((unsigned char) EEPROM.read(address + 1));
  return result;
}

void write_eeprom_short(int address, short value) {
  EEPROM.write(address, ((value >> 8) & 0xFF));
  EEPROM.write(address + 1, (value & 0xFF));
}

/*
 * hard_1_int and hard_2_int are the hardware interrupt handlers which
 * implement a pair of counting registers.  There is no software debouncing.
 * The hardware should include an RC circuit to debounce the signal, where
 * the RC circuit sets the response rate for the data logger.
 */
void hard_1_int() {
    count_1++;
    
    modbus_regs[REG_D0] = 0;
    modbus_regs[REG_D0_CNT] = count_1;
}

void hard_2_int() {
    count_2++;
    
    modbus_regs[REG_D1] = 0;
    modbus_regs[REG_D1_CNT] = count_2;
}

void setup() {
    /*
     * Reset the watchdog.  This is a simple sketch, but if it gets hung, I'd
     * like it to reset itself.
     */
    MCUSR = 0;
    wdt_disable();
    wdt_reset();
    
    /*
     * Make the LED pin an "output" so it can be blinked whenever I receeive a
     * packet, and whenever another second since the last blink has happened.
     */
    pinMode(LED_PIN, OUTPUT);
    digitalWrite(LED_PIN, LOW);
    
    /*
     * Turn off coils 1 through 4.
     */
    pinMode(COIL_1_PIN, OUTPUT);
    digitalWrite(COIL_1_PIN, LOW);
    coil_1_state = 0;
    
    pinMode(COIL_2_PIN, OUTPUT);
    digitalWrite(COIL_2_PIN, LOW);
    coil_2_state = 0;
    
    pinMode(COIL_3_PIN, OUTPUT);
    digitalWrite(COIL_3_PIN, LOW);
    coil_3_state = 0;
    
    pinMode(COIL_3_PIN, OUTPUT);
    digitalWrite(COIL_3_PIN, LOW);
    coil_3_state = 0;
    
    /*
     * Set the digital inputs.
     */
    pinMode(HARD_COUNT_0_PIN, INPUT);
    pinMode(HARD_COUNT_1_PIN, INPUT);
    pinMode(DIGITAL_IN_2, INPUT);
    pinMode(DIGITAL_IN_3, INPUT);
    
    /*
     * Set the two interrupt handlers.
     */
    attachInterrupt(0, hard_1_int, FALLING);
    attachInterrupt(1, hard_2_int, FALLING);
    
    /*
     * Setup the analog inputs.
     */
    pinMode(A0, INPUT);
    digitalWrite(A0,  HIGH);
    pullups[0] = 1;
    
    pinMode(A1, INPUT);
    digitalWrite(A1,  HIGH);
    pullups[1] = 1;
    
    pinMode(A2, INPUT);
    digitalWrite(A2,  LOW);
    pullups[2] = 0;
    
    pinMode(A3, INPUT);
    digitalWrite(A3,  LOW);
    pullups[3] = 0;
    
    pinMode(A4, INPUT);
    digitalWrite(A4, LOW);
    
    pinMode(A5, INPUT);
    digitalWrite(A5, LOW);
    
    /*
     * Get the baud rate.  It is stored at address 64 on a greenSensors box and
     * will be fetched from there.
     */
    modbus_baud = (unsigned short) read_eeprom_short(64);
    if (modbus_baud == -1 || (modbus_baud % 9600) != 0) {
      modbus_baud = 19200;
    }
    
    /*
     * The Modbus specification says that 19200 is the default.
     */
    switch (modbus_baud) {
      case 9600:
        modbus_tmo = 4;
        break;
      case 19200:
        modbus_tmo = 3;
        break;
      case 38400:
        modbus_tmo = 2;
        break;
      case 57600:
        modbus_tmo = 2;
        break;
      default:
        modbus_baud = 19200;
        modbus_tmo = 2;
        break;
    }
    
    modbus_unit = (unsigned short) EEPROM.read(10);
    if (modbus_unit == 0 || modbus_unit == 0xFF)
      modbus_unit = 1;
      
    /*
     * Save these two values in the map.  They are mirrored by the named
     * variables, but this allows the user to change the values with a
     * WRITE REGISTER command.
     */
    modbus_regs[REG_BAUD] = modbus_baud / 9600;
    modbus_regs[REG_UNIT] = modbus_unit;
    
    /*
     * The actual serial number starts at address 4, but has a two character
     * device prefix.  This will return just the number.
     */
    device_serial = read_eeprom_short(6);
    if (device_serial == (unsigned short) 0xFFFF)
      device_serial = 0;
      
    modbus_regs[REG_SERIAL] = device_serial;
    
    /*
     * Now set up the watchdog.  I use 8 seconds because of problems that
     * have been reported in certain boot loaders.  A nice long watchdog
     * will make it easier for someone to wrestle control of their Arduino
     * back.
     */
    MCUSR = 0;
    wdt_enable(WDTO_8S);
    
    /*
     * Now start the serial port.
     */
    Serial.begin(modbus_baud);
}

/*
 * update_eeprom -- Update the EEPROM with any changed Modbus registers that are
 *    stored in EEPROM.
 */
void update_eeprom() {
  if (modbus_unit != modbus_regs[REG_UNIT]) {
    if (modbus_regs[REG_UNIT] > 0 && modbus_regs[REG_UNIT] <= 253) {
      modbus_unit = modbus_regs[REG_UNIT];
      EEPROM.write(10, modbus_unit);
    } else {
      modbus_regs[REG_UNIT] = modbus_unit;
    }
  }
  
  if (modbus_baud != modbus_regs[REG_BAUD] * 9600) {
    if (modbus_regs[REG_BAUD] == 1 || modbus_regs[REG_BAUD] == 2 ||
        modbus_regs[REG_BAUD] == 4 || modbus_regs[REG_BAUD] == 6) {
      modbus_baud = modbus_regs[REG_BAUD] * 9600;
      if (modbus_baud == 9600)
        modbus_tmo = 4;
      else
        modbus_tmo = 2;
        
      write_eeprom_short(64, modbus_baud);
      Serial.begin(modbus_baud);
    } else {
      modbus_regs[REG_BAUD] = modbus_baud / 9600;
    }
  }
  
  if (device_serial == 0 && modbus_regs[REG_SERIAL] != 0) {
    write_eeprom_short(6, modbus_regs[REG_SERIAL]);
    device_serial = modbus_regs[REG_SERIAL];
  } else {
    modbus_regs[REG_SERIAL] = device_serial;
  }
}

void flip_led() {
  digitalWrite(LED_PIN, last_led_state ? LOW:HIGH);
  last_led_state = ! last_led_state;
  
  last_led_flip = millis();
}

/* Start of the Modbus library. */

/*
 * These variables are required for the Modbus library to know its limits.
 */
short  modbus_regs_cnt = MODBUS_REGS;
short  modbus_coils_cnt = MODBUS_COILS;

/* Start of Modbus library code */

/*
 * Modbus exception codes.
 */
enum { 
        EX_ILLEGAL_FUNCTION = 1, 
        EX_ILLEGAL_DATA_ADDRESS = 2, 
        EX_ILLEGAL_DATA_VALUE = 3, 
        EX_SLAVE_DEVICE_FAILURE = 4,
        EX_SLAVE_BUSY = 6,
        EX_MEMORY_PARITY_ERROR = 8
};

/*
 * Supported Modbus functions.
 */
enum {
        FC_READ_COILS = 0x01,
        FC_READ_DISCRETE_INPUTS = 0x02,
        FC_READ_HOLDING_REGISTERS  = 0x03,
        FC_READ_INPUT_REGISTERS = 0x04,
        FC_WRITE_SINGLE_COIL = 0x05,
        FC_WRITE_SINGLE_REGISTER  = 0x06,
#ifdef HAS_COUNTS
        FC_GET_COMM_EVENT_COUNTER = 0x0B,
#endif
#ifdef HAS_LOG
        FC_GET_COMM_EVENT_LOG = 0x0C,
#endif
#ifdef HAS_MULTI
        FC_WRITE_MULTIPLE_COILS = 0x0F,
        FC_WRITE_MULTIPLE_REGISTERS = 0x10,
#endif
#ifdef HAS_SRVR_ID
        FC_REPORT_SERVER_ID = 0x11,
#endif
#ifdef HAS_FILES
        FC_READ_FILE_RECORD = 0x14,
        FC_WRITE_FILE_RECORD = 0x15,
#endif
#ifdef HAS_FIFO
        FC_READ_FIFO = 0x18,
#endif
#ifdef HAS_MEI
        FC_READ_MEI = 0x2B,
#endif
};

/*
 * The last time, in milliseconds, that there was input.
 */
unsigned    long    last_input = 0;

/*
 * Define MAX_PDU to the largest input =or= output packet you expect to
 * see.  Then define MAX_PDU_IN and MAX_PDU_OUT to the input and output
 * limits, respectively.
 */
 
#define  TWO_BUFFERS /* Separate input and output Modbus buffers */

#define  MAX_PDU  256
#define  MAX_PDU_IN 256
#define  MAX_PDU_OUT 256

#if (MAX_PDU_IN > MAX_PDU) || (MAX_PDU_OUT > MAX_PDU)
#error Invalid PDU size!
#endif

#ifndef TWO_BUFFERS
#if MAX_PDU_IN != MAX_PDU_OUT
#error Packet mismatch!
#endif
#endif

unsigned    char    modbus_in[MAX_PDU_IN];
unsigned    short   modbus_in_cnt = 0;

/*
 * The smaller AVR parts have issues with insufficient RAM.  Basic functions are
 * written to finish with the input buffer before starting on the output buffer,
 * to avoid wasting memory.  The more advanced features require both buffers.
 */
#ifdef TWO_BUFFERS
unsigned    char    modbus_out[MAX_PDU_OUT];
#else
unsigned    char    *modbus_out = modbus_in;
#endif
unsigned    short   modbus_out_cnt = 0;

#if defined(HAS_FILES) && !defined(TWO_BUFFERS)
#error Two buffers are required for the READ FILE RECORD feature
#endif

#ifdef HAS_COUNTS
unsigned short event_count;
#ifdef HAS_LOG
unsigned short message_count;
unsigned char  message_bytes[64];
unsigned char  message_bytes_cnt;
unsigned char  message_bytes_wrapped = 0;

void modbus_add_comm_log_entry(unsigned char entry) {
  message_bytes[message_bytes_cnt++] = entry;
  
  if (message_bytes_cnt & ~0x3F) {
      message_bytes_cnt = 0;
      message_bytes_wrapped = 1;
  }
  
  if (entry & 0x80)
    message_count++;
}
#endif
#endif

unsigned char modbus_packet_check() {
    unsigned long  now;
    unsigned char  can_read;
    
    /*
     * Check the number of input bytes and determine if there is any need to
     * see if there's even a packet present.
     */
    can_read = Serial.available();
    if (can_read == 0 && modbus_in_cnt == 0)
      return 0;
      
    /*
     * Either I have one or more bytes to read, or I must look for a timeout.  Do
     * the bytes first.  Either way, I need the current time.
     */
    now = millis();
    if (can_read != 0) {
      
      /*
       * I have data to read.  I must save the current time (for the timeout)
       * then drain the input buffer so I don't get an overrun there.
       */
      last_input = now;
      
      while (can_read > 0) {
        unsigned char data = Serial.read();
        
        if (modbus_in_cnt < MAX_PDU_IN)
          modbus_in[modbus_in_cnt++] = data;

        can_read--;
      }
      return 0;
    }
    
    /*
     * The Modbus standard specifies a packet ends 3.5 character times after the
     * last byte is received.  That's a bit over 3.5ms at 9600 baud, and 1.8ms
     * at 19.2kBaud.
     *
     * NOTE: Many USB-to-Serial converters have major timing issues and conforming
     * to the specification may be very hard and force modbus_tmo to be changed.
     */
    return now - last_input > modbus_tmo;
}

void modbus_put_short(unsigned short value) {
  if (modbus_out_cnt > (MAX_PDU_OUT-2))
    return;
    
  modbus_out[modbus_out_cnt++] = value >> 8;
  modbus_out[modbus_out_cnt++] = value & 0xFF;
}

unsigned short modbus_get_short(unsigned char offset) {
  return(modbus_in[offset] << 8) | modbus_in[offset + 1];
}

unsigned short modbus_crc(unsigned char *data, unsigned char cnt)
{
  unsigned char byte, bit;
  unsigned short result;

  result = 0xFFFF;

  for (byte = 0;byte < cnt;byte++) {
    result ^= data[byte];

    for (bit = 0; bit < 8;bit++) {
      if (result & 0x0001)
        result = (result >> 1) ^ 0xA001;
      else
        result = (result >> 1);
    }
  }
  
  /*
   * The CRC is byte-swapped, so byte swap it on return.
   */
  return ((result >> 8) & 0xFF) | ((result & 0xFF) << 8);
}

void modbus_add_crc() {
  modbus_put_short(modbus_crc(modbus_out, modbus_out_cnt));
}

void modbus_write() {
#ifdef HAS_LOG
  unsigned char log_value = 0x40;    // Generic SEND entry.
#endif
  /*
   * NEVER transmit a broadcast reply.
   */
  if (modbus_out[0] == 0)
    return;
  
#ifdef HAS_LOG
  /*
   * See if this is an exception response.
   */
  if (modbus_out[1] & 0x80)
    log_value |= 0x01;
    
  modbus_add_comm_log_entry(log_value);
#endif
  modbus_add_crc();
  
  delay(modbus_tmo);
  Serial.write(modbus_out, modbus_out_cnt);
  Serial.flush();
  delay(modbus_tmo);
}

void modbus_exception(unsigned char code) {
  /*
   * NEVER report an exception on a broadcast message.
   */
  if (modbus_in[0] == 0)
    return;
    
  /*
   * Copy the command code from the input packet to the output packet,
   * setting bit 0x80 in the process.  Then append the exception code
   * passed in.
   */
   
  unsigned char function = modbus_in[1] | 0x80;
  
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = function;
  modbus_out[modbus_out_cnt++] = code;
  
  modbus_write();
}

unsigned char modbus_check_crc() {
  if (modbus_in_cnt <= 2)
    return 0;
    
  return modbus_crc(modbus_in, modbus_in_cnt - 2) == modbus_get_short(modbus_in_cnt - 2);
}

#ifdef HAS_FIFO
void slave_read_fifo() {
  unsigned short fifo_pointer = modbus_get_short(2);
  char fifo_count;
  
  
  if ((fifo_count = get_fifo_count(fifo_pointer)) < 0 || fifo_count >= 32) {
    modbus_exception(EX_ILLEGAL_DATA_VALUE);
    return;
  }
  
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_READ_FIFO;
  modbus_put_short((fifo_count * sizeof (short)) + sizeof(short));
  modbus_put_short(fifo_count);
  
  for (int i = 0;i < fifo_count;i++)
    modbus_put_short(get_fifo_value(fifo_pointer));
    
#ifdef HAS_COUNTS
  event_count++;
#endif
  modbus_write();
}
#endif

#ifdef HAS_FILES
/**
 * slave_read_file_record - Read one or more records from EEPROM
 */
void slave_read_file_record() {
  unsigned char byte_count = modbus_in[2];
  unsigned char requests = (modbus_in_cnt - 4) / 7;
  int total_registers = 0;
  unsigned char result_size = 0;
  
  if (! (7 <= byte_count && byte_count <= 245)) {
illegal_data_value:
    modbus_exception(EX_ILLEGAL_DATA_VALUE);
    return;
  }
  
  /*
   * There are 5 bytes that aren't part of the request list.
   * The unit number, function code, byte count, and 2 bytes
   * for the CRC.
   */
  if (byte_count != modbus_in_cnt - 5)
    goto illegal_data_value;
  
  for (int i = 0;i < requests;i++) {
    char code = modbus_in[3 + (i * 7)];
    unsigned short file = modbus_get_short(4 + (i * 7));
    unsigned short record = modbus_get_short(6 + (i * 7));
    unsigned short registers = modbus_get_short(8 + (i * 7));
    
    if (code != 6)
      goto illegal_data_value;
      
    if (! file_check_entry(file, record, registers)) {
      modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
      return;
    }
    total_registers += registers;
  }
  
  result_size = (requests * 2) + (total_registers * sizeof(short));
  if (result_size + 2 * sizeof(char) >= 253)
    goto illegal_data_value;
    
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_READ_FILE_RECORD;
  modbus_out[modbus_out_cnt++] = result_size;
  
  for (int i = 0;i < requests;i++) {
    unsigned short file = modbus_get_short(4 + (i * 7));
    unsigned short record = modbus_get_short(6 + (i * 7));
    unsigned short registers = modbus_get_short(8 + (i * 7));
    unsigned short value;
    
    modbus_out[modbus_out_cnt++] = registers * sizeof(short) + 1;
    modbus_out[modbus_out_cnt++] = 6;
    
    for (int j = 0;j < registers;j++) {
      value = file_get_value(file, record, j);
      modbus_put_short(value);
    }
  }
#ifdef HAS_COUNTS
  event_count++;
#endif
  modbus_write();
}

void slave_write_file_record() {
  unsigned char byte_count = modbus_in[2];
  unsigned char requests = (modbus_in_cnt - 4) / 7;
  unsigned char base = 3;
  int total_registers = 0;
  unsigned char result_size = 0;
  int i = 0;
  
  /*
   * There are 5 bytes that aren't part of the request list.
   * The unit number, function code, byte count, and 2 bytes
   * for the CRC.
   */
  if (byte_count < 7 || byte_count != modbus_in_cnt - 5) {
illegal_data_value:
    modbus_exception(EX_ILLEGAL_DATA_VALUE);
    return;
  }
  
  while (base < byte_count) {
    char code = modbus_in[base];
    unsigned short file = modbus_get_short(base + 1);
    unsigned short record = modbus_get_short(base + 3);
    unsigned short registers = modbus_get_short(base + 5);
    
    if (code != 6)
      goto illegal_data_value;
      
    if (! file_check_entry(file, record, registers)) {
      modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
      return;
    }
    base += 7;
    
    for (int j = 0;j < registers;j++)
      file_put_value(file, record, j, modbus_get_short(base + j * 2));
      
    base += registers * 2;
  }
  
  /*
   * This is safe because FILE commands require dual buffers, which is why the copy
   * has to be performed.
   */
  memcpy(modbus_out, modbus_in, modbus_in_cnt);
  modbus_out_cnt = modbus_in_cnt;
  
#ifdef HAS_COUNTS
  event_count++;
#endif
  modbus_write();
}
#endif

/*
 * slave_read_multiple_coils - Read one or more coils, using the get_coil() function.
 */
void slave_read_multiple_coils() {
  unsigned char function = modbus_in[1];
  unsigned short coil_start = modbus_get_short(2);
  unsigned short coil_count = modbus_get_short(4);
  unsigned char byte = 0;
  unsigned char mask = 0x1;
  unsigned char i, j;
  
  if (coil_start >= modbus_coils_cnt || coil_start + coil_count > modbus_coils_cnt || coil_count == 0) {
illegal_data_address:
    modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
    return;
  }
  
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = function;
  modbus_out[modbus_out_cnt++] = (coil_count + 7) >> 3;
  
  /*
   * Process coil values in 1 byte chunks.  The bits are handled inside the loop.
   */
  for (i = 0;i < coil_count;i += 8) {
    byte = 0;
    mask = 1;
    
    /*
     * Now process the bits.
     */
    for (j = i;j < coil_count;j++) {
      char coil_value = get_coil(j + coil_start);
      if (coil_value == (char) -1)
        goto illegal_data_address;
      else if (coil_value != 0)
        byte |= mask;
      
      mask <<= 1;
    }
    modbus_out[modbus_out_cnt++] = byte;
  }
#ifdef HAS_COUNTS
  event_count++;
#endif
  modbus_write();
}

void slave_write_single_coil() {
  unsigned short coil_address = modbus_get_short(2);
  unsigned short coil_value = modbus_get_short(4);
  
  if (coil_value != 0x0000 && coil_value != 0xFF00) {
    modbus_exception(EX_ILLEGAL_DATA_VALUE);
    return;
  }
  
  if (! set_coil(coil_address, coil_value != 0)) {
illegal_data_address:
    modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
    return;
  }
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_WRITE_SINGLE_COIL;
  modbus_put_short(coil_address);
  modbus_put_short(coil_value);
  
#ifdef HAS_COUNTS
  event_count++;
#endif
  modbus_write();
}

#ifdef HAS_COUNTS
void slave_get_comm_event_counter() {
  modbus_out_cnt = 0;
  
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_GET_COMM_EVENT_COUNTER;
  
  /*
   * This isn't a programmable device, so it is never busy.
   */
  modbus_put_short(0);
  modbus_put_short(event_count);
  
  /*
   * This modbus_write() call doesn't count as a message.
   */
  modbus_write();
}
#endif

#ifdef HAS_LOG
void slave_get_comm_event_log() {
  unsigned char log_count;
  unsigned char message_start;
  
  if (message_bytes_wrapped) {
    log_count = 64;
  } else {
    log_count = message_bytes_cnt;
  }
  modbus_out_cnt = 0;
  
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_GET_COMM_EVENT_LOG;
  modbus_out[modbus_out_cnt++] = 6 + log_count; 
  
  /*
   * This isn't a programmable device, so it is never busy.
   */
  modbus_put_short(0);
  modbus_put_short(event_count);
  modbus_put_short(message_count);
  
  for (unsigned char i = log_count;i > 0;i--)
    modbus_out[modbus_out_cnt++] = message_bytes[(message_start + i - 1) & 0x3F];
    
  event_count++;
  modbus_write();
}
#endif

#ifdef HAS_MULTI
/*
 * slave_write_multiple_coils - Write one or more coils, using the set_coil() function.
 */
void slave_write_multiple_coils() {
  unsigned int set_count = 0;
  unsigned short coil_start = modbus_get_short(2);
  unsigned short coil_count = modbus_get_short(4);
  unsigned char  byte_count = modbus_in[6];
  unsigned char byte = 0;
  unsigned char mask = 0x1;
  unsigned char i, j;
  unsigned char is_broadcast = modbus_in[0] == 0;
  
  /*
   * Make sure all the coils can be written before any of the coils are written.
   */
  for (i = 0;i < coil_count;i++) {
    if (! is_coil_setable(coil_start + i)) {
      if (! is_broadcast)
        modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
        
      return;
    }
  }
  
  if ((coil_count + 7) >> 3 != byte_count) {
    if (! is_broadcast)
      modbus_exception(EX_ILLEGAL_DATA_VALUE);
      
    return;
  }
  
  /*
   * Process the coils in bytes, then process the bits inside the loop.
   */
  for (i = 0;i < coil_count;i += 8) {
    byte = modbus_in[7 + (i / 8)];
    mask = 1;
    
    for (j = i;j < coil_count;j++) {
      set_coil(j + coil_start, (byte & mask) ? 1:0);
      mask <<= 1;
    }
  }
  
#ifdef HAS_COUNTS
  event_count++;
#endif
  if (is_broadcast)
    return;
    
  modbus_out_cnt = 0;
  
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_WRITE_MULTIPLE_COILS;
  modbus_put_short(coil_start);
  modbus_put_short(coil_count);
  
  modbus_write();
}
#endif

void slave_read_registers() {
  unsigned short address = modbus_get_short(2);
  unsigned short count = modbus_get_short(4);
  unsigned char i;
  unsigned char function;
  
  /*
   * Unsigned math means never knowing when you're going to wrap ...
   */
  if (address >= modbus_regs_cnt || (address + count) > modbus_regs_cnt) {
    modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
    return;
  }
  function = modbus_in[1];
  
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = function;
  modbus_out[modbus_out_cnt++] = 2 * count;
  
  for (i = 0;i < count;i++) {
    /*
     * Don't let a value change out from under our feet.
     */
    noInterrupts();
    modbus_put_short(modbus_regs[address + i]);
    interrupts();
  }
#ifdef HAS_COUNTS
  event_count++;
#endif
  modbus_write();
}

void slave_write_single_register() {
  unsigned short address = modbus_get_short(2);
  unsigned short value = modbus_get_short(4);
  unsigned char i;
  unsigned char is_broadcast = modbus_in[0] == 0;
  
  /*
   * Unsigned math means never knowing when you're going to wrap ...
   */
  if (address >= modbus_regs_cnt) {
    modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
    return;
  }
  
  modbus_regs[address] = value;
  
#ifdef HAS_COUNTS
  event_count++;
#endif
  if (is_broadcast)
    return;
    
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_WRITE_SINGLE_REGISTER;
  modbus_put_short(address);
  modbus_put_short(value);
  
  modbus_write();
}

#ifdef HAS_MULTI
void slave_write_multiple_registers() {
  unsigned short address = modbus_get_short(2);
  unsigned short count = modbus_get_short(4);
  unsigned char  bytes = modbus_in_[6];
  unsigned char i;
  unsigned char is_broadcast = modbus_in[0] == 0;
  
  /*
   * Unsigned math means never knowing when you're going to wrap ...
   */
  if (address >= modbus_regs_cnt || (address + count) > modbus_regs_cnt) {
    if (! is_broadcast)
      modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
      
    return;
  }
  
  /*
   * Verify the byte count against the register count.
   */
  if (count * 2 != bytes) {
    if (! is_broadcast)
      modbus_exception(EX_ILLEGAL_DATA_VALUE);
      
    return;
  }
  
  for (i = 0;i < count;i++)
    modbus_regs[address + i] = modbus_get_short(6 + (2 * i));
  
#ifdef HAS_COUNTS
  event_count++;
#endif
  if (is_broadcast)
    return;
    
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_WRITE_MULTIPLE_REGISTERS;
  modbus_put_short(address);
  modbus_put_short(count);
  
  modbus_write();
}
#endif

#ifdef HAS_SRVR_ID
void slave_report_server_id() {
  int  object_length;
  
  object_length = strlen_P(product_name);
  
  /*
   * The entire message must fit into the largest permissible packet.
   */
  if (object_length + 5 + sizeof(short) > MAX_PDU_OUT) {
    modbus_exception(EX_ILLEGAL_FUNCTION);
    return;
  }
  
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_REPORT_SERVER_ID;
  modbus_out[modbus_out_cnt++] = 2 + object_length;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = 0xFF;
  
  strcpy_P((char *) modbus_out + modbus_out_cnt, product_name);
  modbus_out_cnt += object_length;

#ifdef HAS_COUNTS
  event_count++;
#endif
  modbus_write();
}
#endif

#ifdef HAS_MEI
void slave_read_device_identification() {
  char          i, j;
  char          cnt;
  char          mode;
  unsigned char object;
  unsigned char total;
  
  /*
   * "Read Device ID" is the only supported function.
   */
  if (modbus_in[2] != 0xE) {
    modbus_exception(EX_ILLEGAL_FUNCTION);
    return;
  }
  
  /*
   * Get the parameters from the packet.
   */
  mode = modbus_in[3];
  object = modbus_in[4];
  
  /*
   * Only read codes 1 (Basic Stream) and 4 (Individual Item) modes
   * are supported.
   */
   
  if (mode != 1 && mode != 4) {
    modbus_exception(EX_ILLEGAL_FUNCTION);
    return;
  }
  
  /*
   * Only objects 0, 1 and 2 are supported.
   */
  if (object > 2) {
    modbus_exception(EX_ILLEGAL_DATA_ADDRESS);
    return;
  }
  
  modbus_out_cnt = 0;
  modbus_out[modbus_out_cnt++] = modbus_unit;
  modbus_out[modbus_out_cnt++] = FC_READ_MEI;
  modbus_out[modbus_out_cnt++] = 0xE;
  modbus_out[modbus_out_cnt++] = mode;
  
  /*
   * This device supports both stream and individual access of the basic
   * fields, only.
   */
  modbus_out[modbus_out_cnt++] = 0x81;
  
  /*
   * It is very important that all three objects defined at the top are
   * able to fit into a single "basic" response.
   */
  if(mode == 1) {
    cnt = 3 - object;
  } else {
    cnt = 1;
  }
  
  total = 7;
  for(i = object, j = cnt;i < 3 && j > 0;i++, j--) {
    int object_length = 0;
    
    switch(i) {
      case 0:
        object_length = strlen_P(company_name);
        break;
      case 1:
        object_length = strlen_P(product_name);
        break;
      case 2:
        object_length = strlen_P(version_text);
        break;
    }
    
    if (total + object_length + 2 > (MAX_PDU_OUT-2))
      break;
      
    total += object_length + 2;
  }
  
  if (j > 0) {
    cnt -= j;
    
    /*
     * The "More Follows" value is "TRUE" and "Next ID" is the
     * first ID that didn't make it.
     */
    modbus_out[modbus_out_cnt++] = 0xFF;
    modbus_out[modbus_out_cnt++] = object + cnt;
  } else {
    /*
     * The "More Follows" and "Next ID" values are always 0 when
     * everything fits.
     */
    modbus_out[modbus_out_cnt++] = 0;
    modbus_out[modbus_out_cnt++] = 0;
  }
  modbus_out[modbus_out_cnt++] = cnt;

  for(i = object, j = cnt;i < 3 && j > 0;i++, j--) {
    int  object_length = 0;
    
    modbus_out[modbus_out_cnt++] = i;
    
    switch(i) {
      case 0:
        object_length = strlen_P(company_name);
        
        modbus_out[modbus_out_cnt++] = strlen_P(company_name);
        strcpy_P((char *) modbus_out + modbus_out_cnt, company_name);
        
        modbus_out_cnt += object_length;
        break;
      case 1:
        object_length = strlen_P(product_name);
        
        modbus_out[modbus_out_cnt++] = object_length;
        strcpy_P((char *) modbus_out + modbus_out_cnt, product_name);
        
        modbus_out_cnt += object_length;
        break;
      case 2:
        object_length = strlen_P(version_text);
        
        modbus_out[modbus_out_cnt++] = object_length;
        strcpy_P((char *) modbus_out + modbus_out_cnt, version_text);
        
        modbus_out_cnt += object_length;
        break;
    }
  }
#ifdef HAS_COUNTS
  event_count++;
#endif
  modbus_write();
}
#endif

/* End of Modbus library code. */

/*
 * User application.
 */
void loop() {
  unsigned char is_broadcast = 0;
  
  /*
   * Pet the nice doggy.
   */
  wdt_reset();
  
  /*
   * I need to know how many loops-per-second.  Because I just do.
   */
  if (++modbus_regs[REG_LOOP_LO] == 0)
    modbus_regs[REG_LOOP_HI]++;
  
  /*
   * Get the current time -- needed for LED blinking and a few other items.
   */
  last_millis = millis();
  
  /*
   * See if I must sample everything.  I do that every 10ms come rain or
   * shine.  Unless it has been more than 15ms, like, I was doing something ...
   */
  if (last_millis - last_sample > 10) {
    modbus_regs[REG_A0] = analogRead(A0);
    modbus_regs[REG_A1] = analogRead(A1);
    modbus_regs[REG_A2] = analogRead(A2);
    modbus_regs[REG_A3] = analogRead(A3);
    modbus_regs[REG_VREF] = analogRead(A5);
    
    /*
     * Get the state of the hardware interrupt input pins.
     */
    modbus_regs[REG_D0] = digitalRead(HARD_COUNT_0_PIN);
    modbus_regs[REG_D1] = digitalRead(HARD_COUNT_1_PIN);
    
    /*
     * Get the state of the other two that don't make interrupts.
     */
    modbus_regs[REG_D2] = digitalRead(DIGITAL_IN_2);
    modbus_regs[REG_D3] = digitalRead(DIGITAL_IN_3);
    
    if (last_millis - last_sample > 15)
      last_sample = last_millis;
    else
      last_sample += 10;
  }
  
  /*
   * A 1Hz signal is output on the LED pin whenever there are no
   * Modbus packets addressed to this unit.
   */
  if (! modbus_packet_check()) {
    /*
     * Manage the LED state for 1Hz blinking.
     */
    if (last_millis - last_led_flip > 500)
      flip_led();
      
    return;
  }
  
  /*
   * Ignore packets with CRC errors and those not addressed to us.
   */
  if (! modbus_check_crc()) {
#ifdef HAS_LOG
    if (modbus_in[0] == modbus_unit) {
      unsigned char log_value = 0x82;
      
      if (modbus_in_cnt == MAX_PDU_IN)
        log_value |= 0x10;
        
      modbus_add_comm_log_entry(log_value);
    }
#endif
    modbus_in_cnt = 0;
    return;
  }
  
  is_broadcast = modbus_in[0] == 0;
  
  if (modbus_unit != modbus_in[0] && ! is_broadcast) {
    modbus_in_cnt = 0;
    return;
  }
  
#ifdef HAS_LOG
  modbus_add_comm_log_entry(0x80 | (is_broadcast ? 0x40:0x00));
#endif
    
  /*
   * The greenSensors box has LEDs for Rx and Tx.  Here I just flip the LED
   * whenever I get a packet.
   */
  flip_led();
  
  switch(modbus_in[1]) {
    case FC_READ_COILS:
    case FC_READ_DISCRETE_INPUTS:
      if (is_broadcast)
        goto done;
        
      slave_read_multiple_coils();
      break;
    case FC_WRITE_SINGLE_COIL:
      slave_write_single_coil();
      break;
#ifdef HAS_COUNTS
    case FC_GET_COMM_EVENT_COUNTER:
      if (is_broadcast)
        goto done;
        
      slave_get_comm_event_counter();
      break;
#endif
#ifdef HAS_LOG
    case FC_GET_COMM_EVENT_LOG:
      if (is_broadcast)
        goto done;
        
      slave_get_comm_event_log();
      break;
#endif
#ifdef HAS_MULTI
    case FC_WRITE_MULTIPLE_COILS:
      slave_write_multiple_coils();
      break;
#endif
    case FC_READ_HOLDING_REGISTERS:
    case FC_READ_INPUT_REGISTERS:
      if (is_broadcast)
        goto done;
        
      slave_read_registers();
      break;
    case FC_WRITE_SINGLE_REGISTER:
      slave_write_single_register();
      
      update_eeprom();
      break;
#ifdef HAS_MULTI
    case FC_WRITE_MULTIPLE_REGISTERS:
      slave_write_multiple_registers();
      
      update_eeprom();
      break;
#endif
#ifdef HAS_SRVR_ID
    case FC_REPORT_SERVER_ID:
      if (is_broadcast)
        goto done;
        
      slave_report_server_id();
      break;
#endif
#ifdef HAS_FIFO
    case FC_READ_FIFO:
      if (is_broadcast)
        goto done;
        
      slave_read_fifo();
      break;
#endif
#ifdef HAS_FILES
    case FC_READ_FILE_RECORD:
      if (is_broadcast)
        goto done;
        
      slave_read_file_record();
      break;
    case FC_WRITE_FILE_RECORD:
      slave_write_file_record();
      break;
#endif
#ifdef HAS_MEI
    case FC_READ_MEI:
      if (is_broadcast)
        goto done;
        
      slave_read_device_identification();
      break;
#endif
    default:
      if (is_broadcast)
        goto done;
        
      modbus_exception(EX_ILLEGAL_FUNCTION);
      break;
  }
done:
  modbus_in_cnt = 0;
}
