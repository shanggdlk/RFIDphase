package org.tagsys.tagsee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

public class TagSee extends Thread implements ReadListener {

	private static Logger logger = Logger.getLogger(TagSee.class);

	private static TagSee _instance;

	private IdentifyListener _listener;

	private List<Observation> _cache = new ArrayList<Observation>();

	@SuppressWarnings("unchecked")
	private Queue<Observation> _queue = (Queue<Observation>) Collections
			.synchronizedList(new LinkedList<Observation>());

	private TagSee() {

		this.start();
	}

	public TagSee instance() {
		if (_instance == null) {
			_instance = new TagSee();
		}

		return _instance;
	}

	public Queue<Observation> getQueue() {

		return this._queue;

	}

	@Override
	public void read(Observation obs) {

		if (obs != null) {
			this.getQueue().add(obs);
		}

	}

	@Override
	public void run() {
		while (true) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

			}

			if (this._queue.size() == 0) {
				continue;
			}

			Observation obs = this._queue.poll();

			if (this._listener != null) {
				if (this._listener.getReportMode() == IdentifyListener.REPORT_EVERY_READ) {
					if (this._cache.size() > 0) {
						this._listener.identify(_cache);
						this._cache.clear();
					}
					this._cache.add(obs);
				} else if (this._listener.getReportMode() == IdentifyListener.REPORT_EVERY_READ_CYCLE) {
					if (_cache.contains(obs)) {
						this._listener.identify(_cache);
						this._cache.clear();
						this._cache.add(obs);
					}
				} else if (this._listener.getReportMode() == IdentifyListener.REPORT_EVERY_ANTENNA_CYCLE) {
					boolean cycle = false;
					for (Observation o : _cache) {
						if (o.getEpc().equals(obs.getEpc())
								&& o.getAntennaId().equals(obs.getAntennaId())) {
							cycle = true;
							break;
						}
					}
					if (cycle) {
						this._listener.identify(_cache);
						this._cache.clear();
					} else {
						this._cache.add(obs);
					}
				}
			}
		}
	}

	public IdentifyListener getListener() {
		return _listener;
	}

	public void setListener(IdentifyListener listener) {
		_listener = listener;
	}

}
