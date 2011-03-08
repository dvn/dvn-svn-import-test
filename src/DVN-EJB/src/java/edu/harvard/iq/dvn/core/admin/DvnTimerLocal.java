/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.iq.dvn.core.admin;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author roberttreacy
 */
public interface DvnTimerLocal {
    void createTimer(Date initialExpiration, long intervalDuration, Serializable info);

    @javax.ejb.Timeout
    @javax.ejb.TransactionAttribute(value = javax.ejb.TransactionAttributeType.NOT_SUPPORTED)
    public void handleTimeout(javax.ejb.Timer timer);

    public void removeHarvestTimer(edu.harvard.iq.dvn.core.vdc.HarvestingDataverse dataverse);

    public void removeExportTimer();

    public void createExportTimer();

    public void createExportTimer(edu.harvard.iq.dvn.core.vdc.VDCNetwork vdcNetwork);

    public void updateHarvestTimer(edu.harvard.iq.dvn.core.vdc.HarvestingDataverse dataverse);


}
