package de.fu_berlin.inf.dpp.core.preferences;

import de.fu_berlin.inf.dpp.core.exceptions.StorageException;

import java.io.IOException;

public interface ISecurePreferences
{
    void flush() throws IOException;

    byte[] getByteArray(String key, byte[] defValue) throws StorageException;

    byte[] getByteArray(String key) throws StorageException;

    boolean getBoolean(String key, boolean defValue) throws StorageException;

    void putBoolean(String key, boolean value, boolean arg2)
            throws StorageException;

    void putByteArray(String key, byte[] value, boolean arg2)
            throws StorageException;
}
