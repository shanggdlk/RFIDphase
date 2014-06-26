package org.tagsys.tagsee;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.custom.messages.IMPINJ_ENABLE_EXTENSIONS;
import org.llrp.ltk.generated.custom.messages.IMPINJ_ENABLE_EXTENSIONS_RESPONSE;
import org.llrp.ltk.generated.custom.parameters.ImpinjFrequencyCapabilities;
import org.llrp.ltk.generated.custom.parameters.ImpinjPeakRSSI;
import org.llrp.ltk.generated.custom.parameters.ImpinjRFPhaseAngle;
import org.llrp.ltk.generated.custom.parameters.ImpinjRFDopplerFrequency;
import org.llrp.ltk.generated.enumerations.GetReaderCapabilitiesRequestedData;
import org.llrp.ltk.generated.enumerations.GetReaderConfigRequestedData;
import org.llrp.ltk.generated.enumerations.StatusCode;
import org.llrp.ltk.generated.messages.ADD_ROSPEC;
import org.llrp.ltk.generated.messages.ADD_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.CLOSE_CONNECTION;
import org.llrp.ltk.generated.messages.CLOSE_CONNECTION_RESPONSE;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES_RESPONSE;
import org.llrp.ltk.generated.messages.GET_READER_CONFIG;
import org.llrp.ltk.generated.messages.GET_READER_CONFIG_RESPONSE;
import org.llrp.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG_RESPONSE;
import org.llrp.ltk.generated.messages.START_ROSPEC;
import org.llrp.ltk.generated.messages.START_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.STOP_ROSPEC;
import org.llrp.ltk.generated.messages.STOP_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.parameters.AntennaConfiguration;
import org.llrp.ltk.generated.parameters.Custom;
import org.llrp.ltk.generated.parameters.EPC_96;
import org.llrp.ltk.generated.parameters.GeneralDeviceCapabilities;
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.generated.parameters.TransmitPowerLevelTableEntry;
import org.llrp.ltk.generated.parameters.UHFBandCapabilities;
import org.llrp.ltk.net.LLRPConnection;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.SignedShort;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.util.Util;

import com.google.gson.Gson;

public class ImpinJReader implements LLRPEndpoint {

	private LLRPConnection _connection;

	protected static Logger _logger = Logger.getLogger(ImpinJReader.class);

	protected ReadListener _readListener;

	private int _messageID = 2;

	private String _ip;

	// default port;
	private int _port = 5084;
	private boolean _isConnected = false;

	public static final int POWER91 = 91;
	public static final int POWER87 = 87;
	public static final int POWER61 = 61;
	public static final int POWER41 = 41;
	public static final int POWER35 = 35;

	public static final int[] FREQUENCIES = new int[] { 920625, 920875, 921125,
			921375, 921625, 921875, 922125, 922375, 922625, 922875, 923125,
			923375, 923625, 923875, 924125, 924375 };

	/**
	 * Report as long as the tag is read.
	 */
	public static final int REPORT_MODE_DEFAULT = 0;
	public static final int REPORT_MODE_EVERY_READ_CYCLE = 1;
	public static final int REPORT_MODE_EVERY_ANTENNA_CYCLE = 2;

	public String getIp() {
		return _ip;
	}

	public void setIp(String ip) {
		_ip = ip;
	}

	public int getPort() {
		return _port;
	}

	public void setPort(int port) {
		_port = port;
	}

	public ImpinJReader() {
		this("localhost");
	}

	public ImpinJReader(String ip, int port) {

		this._ip = ip;
		this._port = port;

	}

	/**
	 * default port 5084
	 * 
	 * @param ip
	 */
	public ImpinJReader(String ip) {
		this(ip, 5084);
	}

	public void connect() throws LLRPConnectionAttemptFailedException {
		try {

			if (this._isConnected) {
				return;
			}

			if (this._connection == null) {
				this._connection = new LLRPConnector(this, this.getIp(),
						this.getPort());
			}

			System.out.println(this.getIp() + ":" + this.getPort());

			_logger.info("Initiate LLRP connection to reader");
			((LLRPConnector) _connection).connect();

			this._isConnected = true;

		} catch (LLRPConnectionAttemptFailedException ex) {
			ex.printStackTrace();
			_logger.error("it fails to connect the readre");
			throw ex;
		}
	}

	public void disconnect() throws InvalidLLRPMessageException,
			TimeoutException {
		LLRPMessage response;
		CLOSE_CONNECTION close = new CLOSE_CONNECTION();
		close.setMessageID(getUniqueMessageID());
		try {
			// don't wait around too long for close
			response = this._connection.transact(close, 3000);

			// check whether ROSpec addition was successful
			StatusCode status = ((CLOSE_CONNECTION_RESPONSE) response)
					.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("CLOSE_CONNECTION was successful");
			} else {
				_logger.info(response.toXMLString());
				_logger.info("CLOSE_CONNECTION Failed ... continuing anyway");
			}
		} catch (InvalidLLRPMessageException ex) {
			_logger.error("CLOSE_CONNECTION: Received invalid response message");
			throw ex;
		} catch (TimeoutException ex) {
			_logger.info("CLOSE_CONNECTION Timeouts ... continuing anyway");
			throw ex;
		} finally {
			this._isConnected = false;
		}

	}

	public void enableImpinjExtensions() {
		LLRPMessage response;

		try {
			_logger.info("IMPINJ_ENABLE_EXTENSIONS ...");
			IMPINJ_ENABLE_EXTENSIONS ena = new IMPINJ_ENABLE_EXTENSIONS();
			ena.setMessageID(getUniqueMessageID());

			response = _connection.transact(ena, 10000);

			StatusCode status = ((IMPINJ_ENABLE_EXTENSIONS_RESPONSE) response)
					.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("IMPINJ_ENABLE_EXTENSIONS was successful");
			} else {
				_logger.info(response.toXMLString());
				_logger.info("IMPINJ_ENABLE_EXTENSIONS Failure");
				System.exit(1);
			}
		} catch (InvalidLLRPMessageException ex) {
			_logger.error("Could not process IMPINJ_ENABLE_EXTENSIONS response");
		} catch (TimeoutException ex) {
			_logger.error("Timeout Waiting for IMPINJ_ENABLE_EXTENSIONS response");
		}
	}

	private UnsignedInteger getUniqueMessageID() {
		return new UnsignedInteger(_messageID++);
	}

	public void setReadListener(ReadListener listener) {
		this._readListener = listener;
	}

	public ReadListener getReadListener() {
		return this._readListener;
	}

	@Override
	public void errorOccured(String arg0) {
		_logger.error(arg0);
		
		System.out.println(arg0);

	}

	@Override
	public synchronized void messageReceived(LLRPMessage message) {
		// convert all messages received to LTK-XML representation
		// and print them to the console

		try {
			_logger.debug("Received " + message.getName()
					+ " message asychronously");

			if (message.getTypeNum() == RO_ACCESS_REPORT.TYPENUM) {
				// The message received is an Access Report.
				RO_ACCESS_REPORT report = (RO_ACCESS_REPORT) message;

				// Get a list of the tags read.
				List<TagReportData> tags = report.getTagReportDataList();
				// Loop through the list and get the EPC of each tag.
				for (TagReportData tag : tags) {

					Observation obs = new Observation();

					obs.setEpc(((EPC_96) tag.getEPCParameter()).getEPC()
							.toString().toUpperCase());

					if (tag.getAntennaID() != null) {
						obs.setAntennaId(tag.getAntennaID().getAntennaID()
								.intValue());
					}

					if (tag.getPeakRSSI() != null) {
						obs.setPeekRssi(tag.getPeakRSSI().getPeakRSSI()
								.intValue());
					}

					if (tag.getChannelIndex() != null) {
						obs.setChannelIndex(tag.getChannelIndex()
								.getChannelIndex().intValue());
					}

					if (tag.getFirstSeenTimestampUTC() != null) {
						obs.setFirstReadTime(tag.getFirstSeenTimestampUTC()
								.getMicroseconds().toLong());
					}

					if (tag.getLastSeenTimestampUTC() != null) {
						obs.setLastReadTime(tag.getLastSeenTimestampUTC()
								.getMicroseconds().toLong());
					}

					if (tag.getTagSeenCount() != null) {
						obs.setReadCount(tag.getTagSeenCount().getTagCount()
								.intValue());
					}
					List<Custom> clist = tag.getCustomList();

					for (Custom cd : clist) {
						if (cd.getClass() == ImpinjRFPhaseAngle.class) {
							obs.setPhase(((ImpinjRFPhaseAngle) cd)
									.getPhaseAngle().toInteger());
						}
						if (cd.getClass() == ImpinjPeakRSSI.class) {
							obs.setPreciseRssi(((ImpinjPeakRSSI) cd).getRSSI()
									.intValue());
						}

						if (cd.getClass() == ImpinjRFDopplerFrequency.class) {
							obs.setDoppler(((ImpinjRFDopplerFrequency) cd)
									.getDopplerFrequency().intValue());
						}

					}

					obs.setTimestamp(System.currentTimeMillis());

					if (this._readListener != null) {
						this._readListener.read(obs);
					}
				}

			} else if (message.getTypeNum() == READER_EVENT_NOTIFICATION.TYPENUM) {
				
				_logger.info(message);
				
				READER_EVENT_NOTIFICATION notify = (READER_EVENT_NOTIFICATION )message;
				if(notify.getReaderEventNotificationData().getAISpecEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getAISpecEvent());
				}
				if(notify.getReaderEventNotificationData().getAntennaEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getAntennaEvent());
				}
				if(notify.getReaderEventNotificationData().getConnectionAttemptEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getConnectionAttemptEvent());
				}
				
				if(notify.getReaderEventNotificationData().getConnectionCloseEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getConnectionCloseEvent());
				}
				
				if(notify.getReaderEventNotificationData().getGPIEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getGPIEvent());
				}
				
				if(notify.getReaderEventNotificationData().getHoppingEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getHoppingEvent());
				}
				
				if(notify.getReaderEventNotificationData().getReaderExceptionEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getReaderExceptionEvent());
				}
				
				if(notify.getReaderEventNotificationData().getReportBufferLevelWarningEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getReportBufferLevelWarningEvent());
				}
				
				if(notify.getReaderEventNotificationData().getReportBufferOverflowErrorEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getReportBufferOverflowErrorEvent());
				}
				if(notify.getReaderEventNotificationData().getROSpecEvent()!=null){
					System.out.println(notify.getReaderEventNotificationData().getROSpecEvent());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void getReaderCapabilities() {
		LLRPMessage response;
		GET_READER_CAPABILITIES_RESPONSE gresp;

		GET_READER_CAPABILITIES get = new GET_READER_CAPABILITIES();
		GetReaderCapabilitiesRequestedData data = new GetReaderCapabilitiesRequestedData(
				GetReaderCapabilitiesRequestedData.All);
		get.setRequestedData(data);
		get.setMessageID(getUniqueMessageID());

		System.out.println("Sending GET_READER_CAPABILITIES message  ...");
		try {
			response = this._connection.transact(get, 10000);

			// check whether GET_CAPABILITIES addition was successful
			gresp = (GET_READER_CAPABILITIES_RESPONSE) response;
			StatusCode status = gresp.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				System.out.println("GET_READER_CAPABILITIES was successful");

				// get the info we need
				GeneralDeviceCapabilities dev_cap = gresp
						.getGeneralDeviceCapabilities();
				if ((dev_cap == null)
						|| (!dev_cap.getDeviceManufacturerName().equals(
								new UnsignedInteger(25882)))) {
					System.out
							.println("DocSample4 must use Impinj model Reader, not "
									+ dev_cap.getDeviceManufacturerName()
											.toString());
				}

				List<Custom> customs = gresp.getCustomList();
				for (Custom custom : customs) {
					if (custom instanceof ImpinjFrequencyCapabilities) {
						ImpinjFrequencyCapabilities cap = (ImpinjFrequencyCapabilities) custom;
						System.out.println(cap.getFrequencyList());
					}
				}

				// System.out.println(dev_cap.get);

				UnsignedInteger modelName = dev_cap.getModelName();
				System.out.println("Found Impinj reader model "
						+ modelName.toString());

				// get the max power level
				if (gresp.getRegulatoryCapabilities() != null) {
					UHFBandCapabilities band_cap = gresp
							.getRegulatoryCapabilities()
							.getUHFBandCapabilities();

					List<TransmitPowerLevelTableEntry> pwr_list = band_cap
							.getTransmitPowerLevelTableEntryList();

					TransmitPowerLevelTableEntry entry = pwr_list.get(pwr_list
							.size() - 1);

					UnsignedShort maxPowerIndex = entry.getIndex();
					SignedShort maxPower = entry.getTransmitPowerValue();
					// LLRP sends power in dBm * 100
					double d = ((double) maxPower.intValue()) / 100;

					System.out.println("Max power " + d + " dBm at index "
							+ maxPowerIndex.toString());
				}
			} else {
				System.out.println(response.toXMLString());
				System.out.println("GET_READER_CAPABILITIES failures");
			}
		} catch (InvalidLLRPMessageException ex) {
			System.out.println("Could not display response string");
		} catch (TimeoutException ex) {
			System.out
					.println("Timeout waiting for GET_READER_CAPABILITIES response");
		}
	}

	public void obtainReaderConfiguration() {
		LLRPMessage response;
		GET_READER_CONFIG_RESPONSE gresp;

		GET_READER_CONFIG get = new GET_READER_CONFIG();
		GetReaderConfigRequestedData data = new GetReaderConfigRequestedData(
				GetReaderConfigRequestedData.All);
		get.setRequestedData(data);
		get.setMessageID(getUniqueMessageID());
		get.setAntennaID(new UnsignedShort(0));
		get.setGPIPortNum(new UnsignedShort(0));
		get.setGPOPortNum(new UnsignedShort(0));

		_logger.info("Sending GET_READER_CONFIG message  ...");
		try {
			response = this._connection.transact(get, 10000);

			// check whether GET_CAPABILITIES addition was successful
			gresp = (GET_READER_CONFIG_RESPONSE) response;
			StatusCode status = gresp.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("GET_READER_CONFIG was successful");

				List<AntennaConfiguration> alist = gresp
						.getAntennaConfigurationList();

				if (!alist.isEmpty()) {
					AntennaConfiguration a_cfg = alist.get(0);
					UnsignedShort channelIndex = a_cfg.getRFTransmitter()
							.getChannelIndex();
					UnsignedShort hopTableID = a_cfg.getRFTransmitter()
							.getHopTableID();
					_logger.info("ChannelIndex " + channelIndex.toString()
							+ " hopTableID " + hopTableID.toString());
				} else {
					_logger.error("Could not find antenna configuration");
				}
			} else {
				_logger.info(response.toXMLString());
				_logger.info("GET_READER_CONFIG failures");
			}
		} catch (InvalidLLRPMessageException ex) {
			_logger.error("Could not display response string");
		} catch (TimeoutException ex) {
			_logger.error("Timeout waiting for GET_READER_CONFIG response");
		}
	}

	public void sendReaderConfiguration(String configFile) {

		SET_READER_CONFIG config = loadReaderConfig(configFile);
		if (config == null) {
			return;
		}
		this.sendReaderConfiguration(config);

	}

	public void sendReaderConfiguration(SET_READER_CONFIG config) {
		LLRPMessage response;

		_logger.info("Loading SET_READER_CONFIG message from file SET_READER_CONFIG.xml ...");

		try {

			response = this._connection.transact(config, 10000);

			// check whetherSET_READER_CONFIG addition was successful
			StatusCode status = ((SET_READER_CONFIG_RESPONSE) response)
					.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("SET_READER_CONFIG was successful");
			} else {
				_logger.info(response.toXMLString());
				_logger.info("SET_READER_CONFIG failures");
			}

		} catch (TimeoutException ex) {
			_logger.error("Timeout waiting for SET_READER_CONFIG response");

		} catch (InvalidLLRPMessageException ex) {
			_logger.error("Unable to convert LTK-XML to Internal Object");
		}

	}

	/**
	 * 
	 * @param roSpecFile
	 * @return thhe RoSpecId
	 */
	public int sendRoSpec(String roSpecFile) {

		ADD_ROSPEC spec = loadRoSpec(roSpecFile);
		return sendRoSpec(spec);
	}

	/**
	 * 
	 * @param addRoSpec
	 * @return The RoSpecId
	 */
	public int sendRoSpec(ADD_ROSPEC addRoSpec) {
		LLRPMessage response;

		addRoSpec.setMessageID(getUniqueMessageID());
		// ROSpec rospec = addRospec.getROSpec();

		_logger.info("Sending ADD_ROSPEC message  ...");
		try {
			response = this._connection.transact(addRoSpec, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((ADD_ROSPEC_RESPONSE) response)
					.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("ADD_ROSPEC was successful");
			} else {
				_logger.info(response.toXMLString());
				_logger.info("ADD_ROSPEC failures");
			}
		} catch (InvalidLLRPMessageException ex) {
			_logger.error("Could not display response string");
		} catch (TimeoutException ex) {
			_logger.error("Timeout waiting for ADD_ROSPEC response");
		}

		return addRoSpec.getROSpec().getROSpecID().intValue();
	}

	public void enableRoSpec(int roSpecId) {
		LLRPMessage response;
		try {
			// factory default the reader
			_logger.info("ENABLE_ROSPEC ...");
			ENABLE_ROSPEC ena = new ENABLE_ROSPEC();
			ena.setMessageID(getUniqueMessageID());
			ena.setROSpecID(new UnsignedInteger(roSpecId));

			response = this._connection.transact(ena, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((ENABLE_ROSPEC_RESPONSE) response)
					.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("ENABLE_ROSPEC was successful");
			} else {
				_logger.error(response.toXMLString());
				_logger.info("ENABLE_ROSPEC_RESPONSE failed ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void startRoSpec(int roSpecId) {
		LLRPMessage response;
		try {
			_logger.info("START_ROSPEC ...");
			START_ROSPEC start = new START_ROSPEC();
			start.setMessageID(getUniqueMessageID());
			start.setROSpecID(new UnsignedInteger(roSpecId));

			response = this._connection.transact(start, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((START_ROSPEC_RESPONSE) response)
					.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("START_ROSPEC was successful");
			} else {
				_logger.error(response.toXMLString());
				_logger.info("START_ROSPEC_RESPONSE failed ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void stopRoSpec(int rospecId) {
		LLRPMessage response;
		try {
			_logger.info("STOP_ROSPEC ...");
			STOP_ROSPEC stop = new STOP_ROSPEC();
			stop.setMessageID(getUniqueMessageID());
			stop.setROSpecID(new UnsignedInteger(rospecId));

			response = this._connection.transact(stop, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((STOP_ROSPEC_RESPONSE) response)
					.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("STOP_ROSPEC was successful");
			} else {
				_logger.error(response.toXMLString());
				_logger.info("STOP_ROSPEC_RESPONSE failed ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			_logger.error("fail to stop the ro spec", e);
		}
	}

	public void deleteRoSpec(int RoSpecId) {
		DELETE_ROSPEC_RESPONSE response;

		System.out.println("Deleting all ROSpecs.");
		DELETE_ROSPEC del = new DELETE_ROSPEC();
		// Use zero as the ROSpec ID.
		// This means delete all ROSpecs.
		del.setROSpecID(new UnsignedInteger(RoSpecId));
		try {
			response = (DELETE_ROSPEC_RESPONSE) this._connection.transact(del,
					10000);
			System.out.println(response.toXMLString());
		} catch (Exception e) {
			System.out.println("Error deleting ROSpec.");
			e.printStackTrace();
		}
	}

	/**
	 * Delete all RoSpecs form the reader
	 * 
	 * @param roSpect
	 */
	public void deleteAllRoSpec() {
		this.deleteRoSpec(0);
	}

	public static ADD_ROSPEC loadRoSpec(String roSpecFile) {
		_logger.info("Loading ADD_ROSPEC message from file ADD_ROSPEC.xml ...");
		try {
			LLRPMessage addRospec = Util
					.loadXMLLLRPMessage(new File(roSpecFile));
			return (ADD_ROSPEC) addRospec;
		} catch (FileNotFoundException ex) {
			_logger.error("Could not find file");
		} catch (IOException ex) {
			_logger.error("IO Exception on file");
		} catch (JDOMException ex) {
			_logger.error("Unable to convert LTK-XML to DOM");
		} catch (InvalidLLRPMessageException ex) {
			_logger.error("Unable to convert LTK-XML to Internal Object");
		}
		return null;
	}

	public static SET_READER_CONFIG loadReaderConfig(String readerConfigFile) {
		try {
			LLRPMessage setConfigMsg = Util.loadXMLLLRPMessage(new File(
					readerConfigFile));
			return (SET_READER_CONFIG) setConfigMsg;
		} catch (FileNotFoundException ex) {
			_logger.error("Could not find file");
		} catch (IOException ex) {
			_logger.error("IO Exception on file");
		} catch (JDOMException ex) {
			_logger.error("Unable to convert LTK-XML to DOM");
		} catch (InvalidLLRPMessageException ex) {
			_logger.error("Unable to convert LTK-XML to Internal Object");
		}
		return null;
	}

	public void factoryDefault() {
		LLRPMessage response;

		try {
			// factory default the reader
			_logger.info("SET_READER_CONFIG with factory default ...");
			SET_READER_CONFIG set = new SET_READER_CONFIG();
			set.setMessageID(getUniqueMessageID());
			set.setResetToFactoryDefault(new Bit(true));
			response = _connection.transact(set, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((SET_READER_CONFIG_RESPONSE) response)
					.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success"))) {
				_logger.info("SET_READER_CONFIG Factory Default was successful");
			} else {
				_logger.info(response.toXMLString());
				_logger.info("SET_READER_CONFIG Factory Default Failure");
				System.exit(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure();

		// Only show root events from the base logger
		Logger.getRootLogger().setLevel(Level.ERROR);

		ImpinJReader reader = new ImpinJReader();
		reader.setIp("192.168.1.212");
		reader.connect();
		reader.deleteAllRoSpec();
		reader.enableImpinjExtensions();
		reader.getReaderCapabilities();
		reader.obtainReaderConfiguration();
		reader.sendReaderConfiguration("specs/SET_READER_CONFIG.xml");
		int roSpec = reader.sendRoSpec("specs/ADD_ROSPEC.xml");
		reader.enableRoSpec(roSpec);
		reader.startRoSpec(roSpec);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		reader.stopRoSpec(roSpec);
		reader.disconnect();
		System.exit(0);

	}

}
