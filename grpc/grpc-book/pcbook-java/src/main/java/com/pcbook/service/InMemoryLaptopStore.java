package com.pcbook.service;

import com.pcbook.pb.Laptop;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryLaptopStore implements LaptopStore {
    private final ConcurrentMap<String, Laptop> data;

    InMemoryLaptopStore() {
        data = new ConcurrentHashMap<>(0);
    }

    @Override
    public void Save(Laptop laptop) throws Exception {
        if (data.containsKey(laptop.getId())) {
            throw new AlreadyExistsException("laptop ID already existing");
        }

        Laptop other = laptop.toBuilder().build();
        data.put(other.getId(), other);
    }
}
