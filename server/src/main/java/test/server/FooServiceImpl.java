package test.server;


import test.api.FooService;
import test.api.RequestDTO;
import test.api.ResponseDTO;

import java.util.Date;

public class FooServiceImpl implements FooService {
    @Override
    public ResponseDTO getFoo(RequestDTO request) throws Exception {
        return null;
    }

    @Override
    public Date getTime() throws Exception {
        return null;
    }

    @Override
    public void sendBytes(byte[] bytes) throws Exception {

    }

    @Override
    public byte[] getBytes() throws Exception {
        return "Hello Hessian".getBytes("UTF-8");
    }
}
