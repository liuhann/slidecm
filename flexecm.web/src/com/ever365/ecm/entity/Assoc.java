package com.ever365.ecm.entity;

import org.bson.types.ObjectId;

import com.ever365.ecm.repo.QName;

public class Assoc {
	private ObjectId src;
	private ObjectId target;
	private QName type;
	private String value;
	public Assoc(ObjectId src, ObjectId target, QName type, String value) {
		super();
		this.src = src;
		this.target = target;
		this.type = type;
		this.value = value;
	}
	public ObjectId getSrc() {
		return src;
	}
	public void setSrc(ObjectId src) {
		this.src = src;
	}
	public ObjectId getTarget() {
		return target;
	}
	public void setTarget(ObjectId target) {
		this.target = target;
	}
	public QName getType() {
		return type;
	}
	public void setType(QName type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
