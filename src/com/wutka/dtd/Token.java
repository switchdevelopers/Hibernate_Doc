package com.wutka.dtd;

/** Token returned by the lexical scanner
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/07/29 05:07:35 $ by $Author: pavels $
 */
class Token
{
	public TokenType type;
	public String value;

	public Token(TokenType aType)
	{
		type = aType;
		value = null;
	}

	public Token(TokenType aType, String aValue)
	{
		type = aType;
		value = aValue;
	}
}
