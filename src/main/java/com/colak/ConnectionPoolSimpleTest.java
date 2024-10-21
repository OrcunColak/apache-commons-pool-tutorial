package com.colak;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.time.Duration;

// See https://medium.com/@ujjawalr/apache-commons-connection-pool-guide-with-example-42be21d14585
@Slf4j
public class ConnectionPoolSimpleTest {

    private static class Connection {
        // Simulated connection class
        public void connect() {
            // connection logic
            System.out.println("Connection established.");
        }

        public void disconnect() {
            System.out.println("Connection closed.");
        }
    }

    private static class MyObjectFactory implements PooledObjectFactory<Connection> {

        @Override
        public PooledObject<Connection> makeObject() {
            return new DefaultPooledObject<>(new Connection());
        }

        @Override
        public void destroyObject(PooledObject<Connection> pooledObject) {
            // Custom destruction logic
        }

        // validateObject() is a callback method, which will be called automatically after the configured interval over all idle objects.
        // You can write your own health check mechanism as per requirement. If this method returns false, the connection will be removed
        // from the pool and based on the configured minimum idle objects, the pool will then add another object.
        @Override
        public boolean validateObject(PooledObject<Connection> pooledObject) {
            // Validation logic
            return true;
        }

        @Override
        public void activateObject(PooledObject<Connection> pooledObject) {
            // Logic to prepare the object for use
        }

        @Override
        public void passivateObject(PooledObject<Connection> pooledObject) {
            // Logic to clean up the object when it goes back to the pool
        }
    }

    public static void main(String[] args) {
        try (GenericObjectPool<Connection> objectPool = createPool()) {

            Connection conn = objectPool.borrowObject();
            conn.connect(); // Use the connection

            System.out.println("Active: " + objectPool.getNumActive());
            System.out.println("Idle: " + objectPool.getNumIdle());

            // Perform operations with the connection...
            conn.disconnect(); // Release resources
            // Return the connection to the pool
            objectPool.returnObject(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static GenericObjectPool<Connection> createPool() {
        MyObjectFactory factory = new MyObjectFactory();
        GenericObjectPool<Connection> objectPool = new GenericObjectPool<>(factory);
        objectPool.setMinIdle(5); // Set minimum idle objects
        objectPool.setMaxTotal(20); // Set maximum active objects
        objectPool.setMaxIdle(10); // Set maximum idle objects
        // test idle objects
        objectPool.setTestWhileIdle(true);
        // health check interval to 3 minutes
        objectPool.setDurationBetweenEvictionRuns(Duration.ofMinutes(3));

        return objectPool;
    }
}
