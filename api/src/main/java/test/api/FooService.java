package test.api;


import java.util.Date;

public interface FooService {

    public ResponseDTO getFoo(RequestDTO request) throws Exception;

    public Date getTime() throws Exception;

    public void sendBytes(byte[] bytes) throws Exception;

    public byte[] getBytes() throws Exception;
}
