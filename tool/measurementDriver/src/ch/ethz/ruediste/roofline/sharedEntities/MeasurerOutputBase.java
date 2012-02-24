package ch.ethz.ruediste.roofline.sharedEntities;

import java.util.UUID;

import ch.ethz.ruediste.roofline.entities.IMeasurer;

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

	@SuppressWarnings("unchecked")
	public <TOutput> TOutput cast(IMeasurer<TOutput> measurer) {
		if (!measurer.getUid().equals(measurerUid)) {
			throw new Error(
					"measurer output was not created from the given measurer");
		}
		return (TOutput) this;
	}

	public <TOutput> boolean isFrom(IMeasurer<TOutput> measurer) {
		return isFrom((MeasurerBase) measurer);
	}

	public boolean isFrom(MeasurerBase measurer) {
		return measurer.getUid().equals(measurerUid);
	}
}
