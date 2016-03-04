

package com.ibm.websphere.dtx.m4pdf;

import com.ibm.websphere.dtx.dtxpi.MAdapter;
import com.ibm.websphere.dtx.dtxpi.MBase;
import com.ibm.websphere.dtx.dtxpi.MConnection;

public class MConnectionImpl extends MConnection
{
    public MConnectionImpl(long lReserved) throws Exception
	{
        super(lReserved);
    }

    public void connect(MAdapter adapter) throws Exception
    {
    }

    public void disconnect() throws Exception
    {
    }

    public void onNotify(int iID, int iParam, MBase Param) throws Exception
    {
    }
}
