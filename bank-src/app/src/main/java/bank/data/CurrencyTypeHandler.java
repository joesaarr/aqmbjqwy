package bank.data;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;

@MappedTypes(Currency.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class CurrencyTypeHandler extends BaseTypeHandler<Currency> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Currency parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getCurrencyCode());
    }

    @Override
    public Currency getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Currency.getInstance(rs.getString(columnName));
    }

    @Override
    public Currency getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Currency.getInstance(rs.getString(columnIndex));
    }

    @Override
    public Currency getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Currency.getInstance(cs.getString(columnIndex));
    }
}
