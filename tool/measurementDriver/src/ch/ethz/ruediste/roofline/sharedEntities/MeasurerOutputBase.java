package ch.ethz.ruediste.roofline.sharedEntities;

import java.util.*;

import ch.ethz.ruediste.roofline.measurementDriver.util.IterableUtils;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public abstract class MeasurerOutputBase extends MeasurerOutputBaseData {
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

	protected abstract void combineImp(MeasurerOutputBase a,
			MeasurerOutputBase b);

	public MeasurerOutputBase combine(MeasurerOutputBase other) {
		if (!getClass().isAssignableFrom(other.getClass())) {
			throw new Error("incompatible types");
		}

		if (!getMeasurerUid().equals(other.getMeasurerUid())) {
			throw new Error("results were generated from different measurers");
		}

		if (getMeasurerId() != other.getMeasurerId()) {
			throw new Error("measurer ids do not match");
		}

		MeasurerOutputBase result;
		try {
			result = getClass().getConstructor().newInstance();
		}
		catch (Exception e) {
			throw new Error(e);
		}
		result.setMeasurerId(getMeasurerId());
		result.setMeasurerUid(getMeasurerUid());

		result.combineImp(this, other);
		return result;
	}

	public static MeasurerOutputBase combine(
			Collection<MeasurerOutputBase> outputs) {
		MeasurerOutputBase head = IterableUtils.head(outputs);

		for (MeasurerOutputBase output : IterableUtils.tail(outputs)) {
			head = head.combine(output);
		}
		return head;
	}
}
