package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.io.IOException;
import java.time.LocalDate;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.Constants;

import lombok.EqualsAndHashCode;

/**
 * <p><u>{@code V4Flight}, version 4 of the data model</u></p>
 * <p>TODO
 * <p>Pros:</p>
 * <ul>
 * <li><p>Codes not text represent the object class in byte stream, smaller</p></li>
 * <li><p>No Java! Interoperable with .Net, C++, etc</p></li>
 * </ul>
 * <p>Cons:</p>
 * <ul>
 * <li><p>A bit more complexity compared to {@link com.hazelcast.nio.serialization.DataSerializable DataSerializable}</p></li>
 * </ul>
 * <p><B>Summary:</B> Worth the extra compared {@link com.hazelcast.nio.serialization.DataSerializable DataSerializable}</p>
 */
@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper=false)
public class V4Flight extends AbstractFlight implements IdentifiedDataSerializable {
	
	/**
	 * <p>Simply write the fields out
	 * </p>
	 * <p>This is the same as for {@link V3Flight#writeData()}.
	 * </p>
	 */
	@Override
	public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
		objectDataOutput.writeUTF(this.getCode());
		objectDataOutput.writeObject(this.getDate());
		objectDataOutput.writeObject(this.getRows());
	}

	/**
	 * <p>Read them back in again
	 * </p>
	 * <p>This is the same as for {@link V3Flight#readData()}.
	 * </p>
	 */
	@Override
	public void readData(ObjectDataInput objectDataInput) throws IOException {
		this.setCode(objectDataInput.readUTF());
		this.setDate((LocalDate)objectDataInput.readObject());
		this.setRows((Person[][])objectDataInput.readObject());
	}


	/**
	 * <p>Which class builds this object on the receiver
	 * </p>
	 */
	@Override
	public int getFactoryId() {
		return Constants.MY_DATASERIALIZABLE_FACTORY;
	}

	/**
	 * <p>The code the factory uses to work out which kind of object to build.
	 * </p>
	 */
	@Override
	public int getId() {
		return Constants.V4FLIGHT_ID;
	}

}
