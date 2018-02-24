package com.routeanalyzer.model;

import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="activities")
public class Activity {
	
	@Id
	private String id;
	private String idUser, device, sport, name, sourceXmlType;
	private Date date;
	@Field
	private SortedSet<Lap> laps;
	
	public Activity(){
		laps = new TreeSet<>();
	}
	
	public String getIdUser() {
		return idUser;
	}
	public void setIdUser(String idUser) {
		this.idUser = idUser;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getSport() {
		return sport;
	}
	public void setSport(String sport) {
		this.sport = sport;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public SortedSet<Lap> getLaps() {
		return Collections.unmodifiableSortedSet(laps);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSourceXmlType() {
		return sourceXmlType;
	}

	public void setSourceXmlType(String sourceXmlType) {
		this.sourceXmlType = sourceXmlType;
	}
	
	public boolean addLap(Lap lap){
		int sizeBeforeAdding = laps.size();
		laps.add(lap);
		return sizeBeforeAdding + 1 == laps.size();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((idUser == null) ? 0 : idUser.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Activity other = (Activity) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (idUser == null) {
			if (other.idUser != null)
				return false;
		} else if (!idUser.equals(other.idUser))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Activity [\n\tid=" + id 
				+ (idUser!=null ? ",\n\t idUser=" + idUser : "") 
				+ (device!=null ? (",\n\t device=" + device) : "")
				+ (sport!=null ? (",\n\t sport=" + sport) : "")
				+ (name!=null ? (",\n\t name=" + name) : "")
				+ (date!=null ? (",\n\t date=" + date) : "")
				+ (laps!=null && !laps.isEmpty() ? ",\n\t laps=" + 
						laps.stream().map(Object::toString).collect(Collectors.joining(",\n\t")):"")
				+ "\n]";
	}
}
