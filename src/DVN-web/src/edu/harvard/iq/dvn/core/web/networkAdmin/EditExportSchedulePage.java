/*
 * Dataverse Network - A web application to distribute, share and analyze quantitative data.
 * Copyright (C) 2007
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation,Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

/*
 * EditNetworkAnnouncementsPage.java
 *
 * Created on October 19, 2006, 4:40 PM
 * 
 */
package edu.harvard.iq.dvn.core.web.networkAdmin;

import com.icesoft.faces.component.ext.HtmlSelectOneMenu;
import edu.harvard.iq.dvn.core.admin.DvnTimerRemote;
import edu.harvard.iq.dvn.core.web.common.VDCBaseBean;
import edu.harvard.iq.dvn.core.web.util.ExceptionMessageWriter;
import edu.harvard.iq.dvn.core.vdc.VDCNetwork;
import edu.harvard.iq.dvn.core.vdc.VDCNetworkServiceLocal;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */

@ViewScoped
@Named("EditExportSchedulePage")
public class EditExportSchedulePage extends VDCBaseBean implements java.io.Serializable  {
    @EJB VDCNetworkServiceLocal vdcNetworkService;
    @EJB (name="dvnTimer")
    DvnTimerRemote remoteTimerService;
    private List <SelectItem> selectExportPeriod = new ArrayList();


     /** 
     * <p>Construct a new Page bean instance.</p>
     */
    public EditExportSchedulePage() {
    }


     /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    public void init() {
        super.init();

        VDCNetwork vdcNetwork = this.getVDCRequestBean().getVdcNetwork();
        exportSchedulePeriod = vdcNetwork.getExportPeriod();
        exportHourOfDay = vdcNetwork.getExportHourOfDay();
        exportDayOfWeek = vdcNetwork.getExportDayOfWeek();

        setSelectExportPeriod(loadSelectExportPeriod());
    }

    public List<SelectItem> loadSelectExportPeriod() {
        List selectItems = new ArrayList<SelectItem>();

        if (this.getVDCRequestBean().getVdcNetwork().getExportPeriod() == null
            || this.getVDCRequestBean().getVdcNetwork().getExportPeriod().equals("")){
            selectItems.add(new SelectItem("", "Not Selected"));
        }

         selectItems.add(new SelectItem("daily", "Export daily"));
         selectItems.add(new SelectItem("weekly", "Export weekly"));

        if ((this.getVDCRequestBean().getVdcNetwork().getExportPeriod() != null)
            && (!this.getVDCRequestBean().getVdcNetwork().getExportPeriod().equals(""))){
            selectItems.add(new SelectItem("none", "Disable export"));
        }

        return selectItems;
    }

    public List<SelectItem> getSelectExportPeriod() {
        return selectExportPeriod;
    }

    public void setSelectExportPeriod(List<SelectItem> selectExportPeriod) {
        this.selectExportPeriod = selectExportPeriod;
    }

    
  
    
    public String save() {

        VDCNetwork vdcnetwork = getVDCRequestBean().getVdcNetwork();
        vdcnetwork.setExportPeriod(exportSchedulePeriod);
        vdcnetwork.setExportHourOfDay(exportHourOfDay);
        if (exportDayOfWeek != null && exportDayOfWeek.intValue()==-1){
            exportDayOfWeek = null;
        }
        vdcnetwork.setExportDayOfWeek(exportDayOfWeek);
        vdcNetworkService.edit(vdcnetwork);
        remoteTimerService.createExportTimer(vdcnetwork);
        getExternalContext().getFlash().put("successMessage","Successfully updated export schedule.");

        return "myNetworkOptions";
     
    }
    
 
    HtmlSelectOneMenu exportPeriod;    
    private String exportSchedulePeriod;
    Integer exportDayOfWeek;
    Integer exportHourOfDay;

    public HtmlSelectOneMenu getExportPeriod() {
        return exportPeriod;
    }

    public void setExportPeriod(HtmlSelectOneMenu exportPeriod) {
        this.exportPeriod = exportPeriod;
    }

    public String getExportSchedulePeriod() {
        return exportSchedulePeriod;
    }

    public void setExportSchedulePeriod(String exportSchedulePeriod) {
        this.exportSchedulePeriod = exportSchedulePeriod;
    }    

    public Integer getExportDayOfWeek() {
        return exportDayOfWeek;
    }

    public void setExportDayOfWeek(Integer exportDayOfWeek) {
        this.exportDayOfWeek = exportDayOfWeek;
    }

    public Integer getExportHourOfDay() {
        return exportHourOfDay;
    }

    public void setExportHourOfDay(Integer exportHourOfDay) {
        this.exportHourOfDay = exportHourOfDay;
    }

    public void validateHourOfDay(FacesContext context,
            UIComponent toValidate,
            Object value) {

        boolean valid = true;


        if (exportPeriod.getLocalValue() != null && (exportPeriod.getLocalValue().equals("daily") || exportPeriod.getLocalValue().equals("weekly"))) {
            if (value == null || ((Integer) value).equals(new Integer(-1))) {
                valid = false;
            }
        }
        if (!valid) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage("This field is required.");
            context.addMessage(toValidate.getClientId(context), message);
        }

    }

    public void validateDayOfWeek(FacesContext context,
            UIComponent toValidate,
            Object value) {

        boolean valid = true;


        if (exportPeriod != null && exportPeriod.getLocalValue() != null && exportPeriod.getLocalValue().equals("weekly")) {
            if (value == null || ((Integer) value).equals(new Integer(-1))) {
                valid = false;
            }
        }
        if (!valid) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage("This field is required.");
            context.addMessage(toValidate.getClientId(context), message);
        }

    }

    public void validateExportPeriod(FacesContext context,
            UIComponent toValidate,
            Object value) {

        boolean valid = true;


        if (((String) value).equals("notSelected")) {
            exportPeriod.setValue("notSelected");
            valid = false;
        }
        if (!valid) {
            ((UIInput) toValidate).setValid(false);
            FacesMessage message = new FacesMessage("This field is required.");
            context.addMessage(toValidate.getClientId(context), message);
        }

    }

    
}

