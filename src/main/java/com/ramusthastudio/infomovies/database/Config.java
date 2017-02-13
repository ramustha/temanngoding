package com.ramusthastudio.infomovies.database;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {
  @Autowired
  Environment mEnv;

  @Bean
  public DataSource getDataSource() {
    String dbUrl = "ec2-107-20-149-243.compute-1.amazonaws.com";
    String username = "cssdjqoqsgnypz";
    String password = "a6a65bff40244971d5f8ea439220e4798c0f818ef06f92b6c4083a8160617190";

    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUrl(dbUrl);
    ds.setUsername(username);
    ds.setPassword(password);

    return ds;
  }

  @Bean(name = "com.linecorp.channel_secret")
  public String getChannelSecret() {
    return mEnv.getProperty("com.linecorp.channel_secret");
  }

  @Bean(name = "com.linecorp.channel_access_token")
  public String getChannelAccessToken() {
    return mEnv.getProperty("com.linecorp.channel_access_token");
  }

  @Bean
  public Dao getPersonDao() {
    return new DaoImpl(getDataSource());
  }
}
