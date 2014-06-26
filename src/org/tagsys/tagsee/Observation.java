package org.tagsys.tagsee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Observation")
public class Observation {

	@Id
	@Column(name = "Id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long _id;

	@Column(name = "epc")
	private String _epc;

	@Column(name = "antennaId")
	private Integer _antennaId;

	@Column(name = "channelIndex")
	private Integer _channelIndex;

	@Column(name = "doppler")
	private Integer _doppler;

	@Column(name = "peekRssi")
	private Integer _peekRssi;

	@Column(name = "preceiseRssi")
	private Integer _preciseRssi;

	@Column(name = "phase")
	private Integer _phase;

	@Column(name = "firstReadTime")
	private Long _firstReadTime;

	@Column(name = "lastReadTime")
	private Long _lastReadTime;

	@Column(name = "readCount")
	private Integer _readCount;

	@Column(name = "timestamp")
	private Long _timestamp;

	public Long getId() {
		return _id;
	}

	public void setId(Long id) {
		_id = id;
	}

	public String getEpc() {
		return _epc;
	}

	public void setEpc(String epc) {
		_epc = epc;
	}

	public Integer getAntennaId() {
		return _antennaId;
	}

	public void setAntennaId(Integer antennaId) {
		_antennaId = antennaId;
	}

	public Integer getChannelIndex() {
		return _channelIndex;
	}

	public void setChannelIndex(Integer channelIndex) {
		_channelIndex = channelIndex;
	}

	public Integer getDoppler() {
		return _doppler;
	}

	public void setDoppler(Integer doppler) {
		_doppler = doppler;
	}

	public Integer getPeekRssi() {
		return _peekRssi;
	}

	public void setPeekRssi(Integer peekRssi) {
		_peekRssi = peekRssi;
	}

	public Integer getPreciseRssi() {
		return _preciseRssi;
	}

	public void setPreciseRssi(Integer preciseRssi) {
		_preciseRssi = preciseRssi;
	}

	public Integer getPhase() {
		return _phase;
	}

	public void setPhase(Integer phase) {
		_phase = phase;
	}

	public Long getFirstReadTime() {
		return _firstReadTime;
	}

	public void setFirstReadTime(Long firstReadTime) {
		_firstReadTime = firstReadTime;
	}

	public Long getLastReadTime() {
		return _lastReadTime;
	}

	public void setLastReadTime(Long lastReadTime) {
		_lastReadTime = lastReadTime;
	}

	public Integer getReadCount() {
		return _readCount;
	}

	public void setReadCount(Integer readCount) {
		_readCount = readCount;
	}

	public Long getTimestamp() {
		return _timestamp;
	}

	public void setTimestamp(Long timestamp) {
		_timestamp = timestamp;
	}

	@Override
	public boolean equals(Object object) {

		Observation obs = (Observation) object;

		if (obs == null) {
			return false;
		}

		if (this.getEpc() == obs.getEpc()) {
			return true;
		}

		return this.getEpc().equals(obs.getEpc());
	}

	@Override
	public int hashCode() {
		return this.getEpc().hashCode();
	}

	public static String propertyHeads() {
		return "EPC," + "AntnenaId," + "ChannelIndex," + "Doppler,"
				+ "PeekRssi," + "PrecissRssi," + "Phase," + "FirstReadTime,"
				+ "LastReadTime," + "ReadCount," + "Timestamp";
	}

	@Override
	public String toString() {
		return this.getEpc() + "," + this.getAntennaId() + ","
				+ this.getChannelIndex() + "," + this.getDoppler() + ","
				+ this.getPeekRssi() + "," + this.getPreciseRssi() + ","
				+ this.getPhase() + "," + this.getFirstReadTime() + ","
				+ this.getLastReadTime() + "," + this.getReadCount() + ","
				+ this.getTimestamp();
	}

	public static void main(String[] args) {
		System.out.println(new Observation() + ",channelIndex:");
	}

}
