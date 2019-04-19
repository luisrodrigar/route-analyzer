package com.routeanalyzer.api.model;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "activities")
public class Activity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	private String idUser;
	private String device;
	private String sport;
	private String name;
	private String sourceXmlType;
	private LocalDateTime date;
	@Field
	@Builder.Default
	private List<Lap> laps = Lists.newArrayList();

	public boolean addLap(Lap lap){
		int sizeBeforeAdding = laps.size();
		laps.add(lap);
		return sizeBeforeAdding + 1 == laps.size();
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Activity activity = (Activity) o;
		return Objects.equals(id, activity.id) &&
				sourceXmlType.equals(activity.sourceXmlType) &&
				date.equals(activity.date) &&
				Objects.equals(laps, activity.laps);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, sourceXmlType, date, laps);
	}
}
