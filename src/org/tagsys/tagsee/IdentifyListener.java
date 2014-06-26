package org.tagsys.tagsee;

import java.util.List;

public interface IdentifyListener {

	public final static int REPORT_EVERY_READ = 0;
	public final static int REPORT_EVERY_READ_CYCLE = 1;
	public final static int REPORT_EVERY_ANTENNA_CYCLE = 2;
	
	public int getReportMode();
	
	public void identify(List<Observation> observations);
}
