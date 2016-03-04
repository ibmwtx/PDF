

package com.ibm.websphere.dtx.m4pdf;

import com.ibm.websphere.dtx.dtxpi.MAdapterSettings;
import com.ibm.websphere.dtx.dtxpi.MConstants;

final class MAdapterSettingsImpl implements MAdapterSettings
{
    public String getCommandMask()
    {
        return "-N%";
    }

    public int getTransMode()
    {
        return MConstants.MPI_TRANSACTIONS_MULTIPLE;
    }

    public int getBurstType()
    {
        return MConstants.MPI_UNITOFWORK_MESSAGE;
    }

    public int getListener()
    {
        return MConstants.MPI_FALSE;
    }

    public int getCombinedListener()
    {
        return MConstants.MPI_FALSE;
    }

    public int getListenerBlocks()
    {
        return MConstants.MPI_FALSE;
    }

    public int getListenerTrans()
    {
        return MConstants.MPI_FALSE;
    }

    public int getRetries()
    {
        return MConstants.MPI_FALSE;
    }

    // Source settings
    public int getSourceManage()
    {
        return MConstants.MPI_FALSE;
    }

    public int getSourceWarnings()
    {
        return MConstants.MPI_TRUE;
    }

    public int getSourceScopes()
    {
        return MConstants.MPI_SCOPE_CARD | MConstants.MPI_SCOPE_BURST | MConstants.MPI_SCOPE_CARD;
    }

    public int getSourceOnSuccess()
    {
        return MConstants.MPI_ACTION_DELETE; 
    }

    public int getSourceOnFailure()
    {
        return MConstants.MPI_ACTION_COMMIT | MConstants.MPI_ACTION_ROLLBACK;
    }

    public int getDefSourceScope()
    {
        return MConstants.MPI_SCOPE_MAP;
    }

    public int getDefSourceOnSuccess()
    {
        return MConstants.MPI_ACTION_DELETE;
    }

    public int getDefSourceOnFailure()
    {
        return MConstants.MPI_ACTION_ROLLBACK;
    }

    // Target settings
    public int getTargetManage()
    {
        return MConstants.MPI_FALSE;
    }

    public int getTargetWarnings()
    {
        return MConstants.MPI_TRUE;
    }

    public int getTargetScopes()
    {
        return MConstants.MPI_SCOPE_CARD | MConstants.MPI_SCOPE_BURST | MConstants.MPI_SCOPE_CARD;
    }

    public int getTargetOnSuccess()
    {
        return MConstants.MPI_ACTION_CREATE | MConstants.MPI_ACTION_CREATEONCONTENT ;
    }

    public int getTargetOnFailure()
    {
        return MConstants.MPI_ACTION_COMMIT | MConstants.MPI_ACTION_ROLLBACK;
    }

    public int getDefTargetScope()
    {
        return MConstants.MPI_SCOPE_MAP;
    }

    public int getDefTargetOnSuccess()
    {
        return MConstants.MPI_ACTION_CREATE;
    }

    public int getDefTargetOnFailure()
    {
        return MConstants.MPI_ACTION_ROLLBACK;
    }
}
