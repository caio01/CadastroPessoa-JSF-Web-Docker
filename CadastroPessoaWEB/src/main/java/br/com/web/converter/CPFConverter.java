package br.com.web.converter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("cpfConverter")
public class CPFConverter implements Converter {

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return null;
		}

		String cpf = (String) value;

		return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
	}

	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null) {
			return null;
		}

		try {
			String cpfString = value.substring(0, 3) + value.substring(4, 7) + value.substring(8, 11) + value.substring(12, 14);

			return cpfString;
		} catch (IndexOutOfBoundsException e) {
			FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Problemas na conversão do cpf.",
					"Devem ser informados apenas números.");

			throw new ConverterException(facesMessage);
		}
	}
}