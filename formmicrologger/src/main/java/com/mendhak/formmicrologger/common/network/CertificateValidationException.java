/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of formmicrologger.
 *
 * formmicrologger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * formmicrologger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with formmicrologger.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.formmicro.gpslogger.common.network;

import java.security.cert.X509Certificate;


public class CertificateValidationException extends RuntimeException {

    private X509Certificate certificate;

    public CertificateValidationException(X509Certificate certificate, String message, Throwable t){
        super(message, t);
        this.certificate = certificate;
    }

    public X509Certificate getCertificate(){
        return certificate;
    }

}