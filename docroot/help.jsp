<%
/**************************************************************************
Copyright (c) 2011-2015:
Istituto Nazionale di Fisica Nucleare (INFN), Italy
Consorzio COMETA (COMETA), Italy

See http://www.infn.it and and http://www.consorzio-cometa.it for details on 
the copyright holders.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author <a href="mailto:giuseppe.larocca@ct.infn.it">Giuseppe La Rocca</a>
****************************************************************************/
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects/>

<script type="text/javascript">
    $(document).ready(function() {
              
    $('.slideshow').cycle({
	fx: 'fade' // choose your transition type (fade, scrollUp, shuffle, etc)
    });
    
    // Roller
    $('#gromacs_footer').rollchildren({
        delay_time         : 3000,
        loop               : true,
        pause_on_mouseover : true,
        roll_up_old_item   : true,
        speed              : 'slow'   
    });
    
    //var $tumblelog = $('#tumblelog');  
    $('#tumblelog').imagesLoaded( function() {
      $tumblelog.masonry({
        columnWidth: 440
      });
    });
});
</script>
                    
<br/>

<fieldset>
<legend>About NUCLEMD</legend>

<section id="content">

<div id="tumblelog" class="clearfix">
    
  <div class="story col3">
  <!--img width="250" src="<%=renderRequest.getContextPath()%>/images/GROMACS_logo.png"/><br/><br/-->
  <p style="text-align:justify; position: relative;">
  <u>NUCLEMD</u> is a computer code based on the Constrained Molecular Dynamics (CoMD) model. 
  The peculiarity of the algorithm consists on the isospin dependence of the nucleon-nucleon cross section and on the
  presence of the Majorana Exchange Operator in the nucleon-nucleon collision term.
  The code will be devoted to the study of Single and Double Charge Exchange processes in nuclear reactions at low and
  intermediate energies.<br/>
  <br/>In particular, in the first stage of simulations, the following reactions will be under study:<br/>
  1.)&nbsp;<sup>18</sup>O + <sup>40</sup>Ca   &nbsp;&nbsp;E<sub><small>inc</small></sub>=270 MeV<br/>
  2.)&nbsp;<sup>18</sup>O + <sup>12,13</sup>C &nbsp;&nbsp;E<sub><small>in</small></sub>=84 MeV
  <br/><br/>
  The aim is to provide theoretical support to the experimental results of the DREAMS collaboration obtained by means of
  the <a href="http://www.lns.infn.it/magnex/descript.html">
  <img width="90" src="<%=renderRequest.getContextPath()%>/images/magnex_logo.gif"/></a> spectrometer.
  </p>
  </div>
                                     
  <div class="story col3" style="font-family: Tahoma,Verdana,sans-serif,Arial; font-size: 14px;">
      <h2>
      <a href="mailto:info@gianluca.giuliani1@gmail.com">
      <img width="100" 
           src="<%= renderRequest.getContextPath()%>/images/contact6.jpg" 
           title="Get in touch with the project"/></a>Contacts
      </h2>
      <p style="text-align:justify;">Gianluca GIULIANI (INFN)<i> &mdash; (Principal Investigator and Key Contact)</i></p>
      <p style="text-align:justify;">Giuseppe LA ROCCA (INFN)<i> &mdash; (Responsible for deployment)</i></p>      
  </div>               
    
  <div class="story col3" style="font-family: Tahoma,Verdana,sans-serif,Arial; font-size: 13px;">
        <h2>Sponsors & Credits</h2>
        <table border="0">                        
            <tr>
                
            <!--td>
            <p align="justify">
            <a href="http://www.dfa.unict.it/">
                <img align="center" width="110"                      
                     src="<%= renderRequest.getContextPath()%>/images/logo_UNICATANIA.png" 
                     border="0" title="Department of Physics adn Astronomy of the University of Catania" />
            </a>
            </p>
            </td>
            
            <td>&nbsp;&nbsp;</td-->
            
            <td>
            <p align="justify">
            <a href="http://www.lns.it/">
                <img align="center" width="150"                      
                     src="<%= renderRequest.getContextPath()%>/images/INFN-LNS.png" 
                     border="0" title="Italian National Institute of Nuclear Physics (INFN-LNS)" />
            </a>
            </p>
            </td>
            
            <td>&nbsp;&nbsp;</td>
            
            <td>
            <p align="justify">
            <a href="https://www.chain-project.eu/">
                <img align="center" width="150"                      
                     src="<%= renderRequest.getContextPath()%>/images/chain-logo-220x124.png" 
                     border="0" title="The CHAIN-REDS Project Home Page" />
            </a>
            </p>
            </td>
            
            <!--td>&nbsp;&nbsp;</td>
            <td>&nbsp;&nbsp;</td>
            
            <td>
            <p align="justify">
            <a href="http://www.gromacs.org/">
                <img align="center" width="250"                      
                     src="<%= renderRequest.getContextPath()%>/images/GROMACS_logo.png" 
                     border="0" title="The GROMACS Official Home Page" />
            </a>
            </p>
            </td-->
                        
            </tr>                                  
        </table>   
  </div>
</div>
</section>
</fieldset>           
                     
<div id="gromacs_footer" style="width:690px; font-family: Tahoma,Verdana,sans-serif,Arial; font-size: 14px;">
    <div>NUCLEMD portlet ver. 1.1.2</div>
    <div>Italian National Institute of Nuclear Physics (INFN), Division of Catania, Italy</div>
    <div>Department of Physics and Astronomy of the University of Catania, Italy</div>
    <div>Copyright © 2014 - 2015. All rights reserved</div>
    <div>This work has been partially supported by
    <a href="http://www.chain-project.eu/">
    <img width="45" 
         border="0"
         src="<%= renderRequest.getContextPath()%>/images/chain-logo-220x124.png" 
         title="The CHAIN-REDS EU FP7 Project"/>
    </a>
    </div>
</div>