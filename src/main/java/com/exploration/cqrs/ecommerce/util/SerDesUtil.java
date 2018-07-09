package com.exploration.cqrs.ecommerce.util;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.nustaq.serialization.simpleapi.DefaultCoder;

import com.exploration.cqrs.ecommerce.command.Command;

/**
 * Serializer with FST: Fast Java serialization 
 */
public class SerDesUtil implements Deserializer<Object>, Serializer<Object> {

	private static DefaultCoder coder = new DefaultCoder(false);

	@Override
	public byte[] serialize(String topic, Object data) {
		return coder.toByteArray(data);
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public Object deserialize(String topic, byte[] data) {
		return coder.toObject(data);
	}

	@Override
	public void close() {
	}
}
