package org.tagsys.tagsee.utils;

import org.llrp.ltk.generated.custom.enumerations.ImpinjFixedFrequencyMode;
import org.llrp.ltk.generated.custom.enumerations.ImpinjInventorySearchType;
import org.llrp.ltk.generated.custom.enumerations.ImpinjLowDutyCycleMode;
import org.llrp.ltk.generated.custom.enumerations.ImpinjPeakRSSIMode;
import org.llrp.ltk.generated.custom.enumerations.ImpinjRFDopplerFrequencyMode;
import org.llrp.ltk.generated.custom.enumerations.ImpinjRFPhaseAngleMode;
import org.llrp.ltk.generated.custom.enumerations.ImpinjSerializedTIDMode;
import org.llrp.ltk.generated.custom.parameters.ImpinjEnablePeakRSSI;
import org.llrp.ltk.generated.custom.parameters.ImpinjEnableRFDopplerFrequency;
import org.llrp.ltk.generated.custom.parameters.ImpinjEnableRFPhaseAngle;
import org.llrp.ltk.generated.custom.parameters.ImpinjEnableSerializedTID;
import org.llrp.ltk.generated.custom.parameters.ImpinjFixedFrequencyList;
import org.llrp.ltk.generated.custom.parameters.ImpinjFrequencyCapabilities;
import org.llrp.ltk.generated.custom.parameters.ImpinjInventorySearchMode;
import org.llrp.ltk.generated.custom.parameters.ImpinjLowDutyCycle;
import org.llrp.ltk.generated.custom.parameters.ImpinjTagReportContentSelector;
import org.llrp.ltk.generated.enumerations.ROReportTriggerType;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG;
import org.llrp.ltk.generated.parameters.AntennaConfiguration;
import org.llrp.ltk.generated.parameters.C1G2EPCMemorySelector;
import org.llrp.ltk.generated.parameters.C1G2InventoryCommand;
import org.llrp.ltk.generated.parameters.C1G2RFControl;
import org.llrp.ltk.generated.parameters.C1G2SingulationControl;
import org.llrp.ltk.generated.parameters.ROReportSpec;
import org.llrp.ltk.generated.parameters.TagReportContentSelector;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.TwoBitField;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedIntegerArray;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray;

public class ReaderConfigUtils {
		
	public static SET_READER_CONFIG creatSET_READER_CONFIG(){
		SET_READER_CONFIG config = new SET_READER_CONFIG();
		
		//reset to factory default
		config.setResetToFactoryDefault(new Bit(false));
		
		//antenna configuration
		AntennaConfiguration antennaConfig = new AntennaConfiguration();
		antennaConfig.setAntennaID(new UnsignedShort(0));
		//c1g2
		C1G2InventoryCommand c1g2 = ReaderConfigUtils.createC1G2InventoryCommand();
		antennaConfig.addToAirProtocolInventoryCommandSettingsList(c1g2);
		config.addToAntennaConfigurationList(antennaConfig);		
		
		//ro report spec
		config.setROReportSpec(ReaderConfigUtils.creatROReportSpec());
		
		return config;
	}
	
	public static C1G2InventoryCommand createC1G2InventoryCommand(){
		C1G2InventoryCommand c1g2 = new C1G2InventoryCommand();
		c1g2.setTagInventoryStateAware(new Bit(false));
		//c1g2 rf control
		C1G2RFControl c1g2Control = new C1G2RFControl();
		c1g2Control.setModeIndex(new UnsignedShort(0));
		c1g2Control.setTari(new UnsignedShort(0));
		c1g2.setC1G2RFControl(c1g2Control);
		//c1g2 singulation control
		C1G2SingulationControl c1g2Singulation = new C1G2SingulationControl();
		c1g2Singulation.setSession(new TwoBitField("2"));
		c1g2Singulation.setTagPopulation(new UnsignedShort(32));
		c1g2Singulation.setTagTransitTime(new UnsignedInteger(0));
		c1g2.setC1G2SingulationControl(c1g2Singulation);
		
		//custom begin
		//impinj inventory search mode
		ImpinjInventorySearchMode searchMode = new ImpinjInventorySearchMode();
		searchMode.setInventorySearchMode(new ImpinjInventorySearchType(ImpinjInventorySearchType.Dual_Target));
		c1g2.addToCustomList(searchMode);
		
		//low duty circle
		ImpinjLowDutyCycle dutyCycle = new ImpinjLowDutyCycle();
		dutyCycle.setLowDutyCycleMode(new ImpinjLowDutyCycleMode(ImpinjLowDutyCycleMode.Disabled));
		dutyCycle.setEmptyFieldTimeout(new UnsignedShort(10000));
		dutyCycle.setFieldPingInterval(new UnsignedShort(200));
		c1g2.addToCustomList(dutyCycle);
		
		//frequency
		ImpinjFixedFrequencyList flist = new ImpinjFixedFrequencyList();
		flist.setFixedFrequencyMode(new ImpinjFixedFrequencyMode(ImpinjFixedFrequencyMode.Channel_List));
		UnsignedShortArray sArray = new UnsignedShortArray();
		sArray.add(new UnsignedShort(1));
		sArray.add(new UnsignedShort(2));
		sArray.add(new UnsignedShort(3));
		sArray.add(new UnsignedShort(4));
		sArray.add(new UnsignedShort(5));
		sArray.add(new UnsignedShort(6));
		sArray.add(new UnsignedShort(7));
		sArray.add(new UnsignedShort(8));
		sArray.add(new UnsignedShort(9));
		sArray.add(new UnsignedShort(10));
		sArray.add(new UnsignedShort(11));
		sArray.add(new UnsignedShort(12));
		sArray.add(new UnsignedShort(13));
		sArray.add(new UnsignedShort(14));
		sArray.add(new UnsignedShort(15));
		sArray.add(new UnsignedShort(16));
		flist.setChannelList(sArray);
//		ImpinjFrequencyCapabilities fCapa = new ImpinjFrequencyCapabilities();
//		UnsignedIntegerArray iArray = new UnsignedIntegerArray();
//		iArray.add(new UnsignedInteger(920625));
//		iArray.add(new UnsignedInteger(920875));
//		iArray.add(new UnsignedInteger(921125));
//		iArray.add(new UnsignedInteger(921375));
//		fCapa.setFrequencyList(iArray);
		c1g2.addToCustomList(flist);
//		c1g2.addToCustomList(fCapa);
		
		return c1g2;
	}
	
	public static ROReportSpec creatROReportSpec(){
		ROReportSpec roReport = new ROReportSpec();
		
		roReport.setROReportTrigger(new ROReportTriggerType(ROReportTriggerType.Upon_N_Tags_Or_End_Of_ROSpec));
		roReport.setN(new UnsignedShort(1));
		
		//TagReportContentSelector
		TagReportContentSelector contentSelector = new TagReportContentSelector();
		contentSelector.setEnableROSpecID(new Bit(false));
		contentSelector.setEnableSpecIndex(new Bit(false));
		contentSelector.setEnableInventoryParameterSpecID(new Bit(false));
		contentSelector.setEnableAntennaID(new Bit(true));
		contentSelector.setEnableChannelIndex(new Bit(true));
		contentSelector.setEnablePeakRSSI(new Bit(true));
		contentSelector.setEnableFirstSeenTimestamp(new Bit(true));
		contentSelector.setEnableLastSeenTimestamp(new Bit(false));
		contentSelector.setEnableTagSeenCount(new Bit(false));
		contentSelector.setEnableAccessSpecID(new Bit(false));
		C1G2EPCMemorySelector epcSelector = new C1G2EPCMemorySelector();
		epcSelector.setEnableCRC(new Bit(false));
		epcSelector.setEnablePCBits(new Bit(false));
		contentSelector.addToAirProtocolEPCMemorySelectorList(epcSelector);
		roReport.setTagReportContentSelector(contentSelector);
		
		//impinj tag report content selector
		ImpinjTagReportContentSelector impinjSelector = new ImpinjTagReportContentSelector();
		ImpinjEnableSerializedTID tid = new ImpinjEnableSerializedTID();
		tid.setSerializedTIDMode(new ImpinjSerializedTIDMode(ImpinjSerializedTIDMode.Disabled));
		impinjSelector.setImpinjEnableSerializedTID(tid);
		
		ImpinjEnableRFPhaseAngle angle = new ImpinjEnableRFPhaseAngle();
		angle.setRFPhaseAngleMode(new ImpinjRFPhaseAngleMode(ImpinjRFPhaseAngleMode.Enabled));
		impinjSelector.setImpinjEnableRFPhaseAngle(angle);
		
		ImpinjEnablePeakRSSI rssi = new ImpinjEnablePeakRSSI();
		rssi.setPeakRSSIMode(new ImpinjPeakRSSIMode(ImpinjPeakRSSIMode.Enabled));
		impinjSelector.setImpinjEnablePeakRSSI(rssi);
		
		//doppler
		ImpinjEnableRFDopplerFrequency doppler = new ImpinjEnableRFDopplerFrequency();
		doppler.setRFDopplerFrequencyMode(new ImpinjRFDopplerFrequencyMode(ImpinjRFDopplerFrequencyMode.Enabled));
		impinjSelector.setImpinjEnableRFDopplerFrequency(doppler);
		
		roReport.addToCustomList(impinjSelector);
		
		return roReport;
	}
}
