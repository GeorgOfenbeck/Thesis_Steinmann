package ch.ethz.ruediste.roofline.dom;

import java.util.UUID;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class MeasurerOutputBase extends MeasurerOutputBaseData {
	@XStreamOmitField
	private UUID measurerUid;

	public UUID getMeasurerUid() {
		return measurerUid;
	}

	public void setMeasurerUid(UUID measurerUid) {
		this.measurerUid = measurerUid;
	}
}
