require 'selenium-webdriver'
require 'appium_lib'
require_relative '../../vendor/gems/appium_lib-8.0.2/lib/appium_lib/driver'


driver = start_selenium_driver

bool_status = false
puts "Hooks Ruby"
Before do
  @driver = driver
  puts "Driver=#{@driver.to_s}"
end

After do |scenario|
  if scenario.passed?
    bool_status = true
  end

  if scenario.failed?
      take_screenshot(scenario, @driver)
      puts "Scenario failed"
      @driver.reset unless ENV['TARGET'] == 'web'
      sleep 2
      on(Signup).singnuptoYona
      sleep 2
      on(Settings).minimizeandrelaunchapp
    # # TODO: close_all_windows_except_main(@driver) if ENV['PLATFORM'] == 'web'
  else
    sleep 2
    on(Settings).minimizeandrelaunchapp
    sleep 2
  end

  # @driver.quit
  # driver = start_selenium_driver
  # @driver.reset unless ENV['TARGET'] == 'web'
  # if ENV['TARGET'] != 'web'
  #   driver.quit
  #   driver = start_selenium_driver
  # end

end


at_exit do
  if ENV['TARGET'] != 'sauce' && ENV['TARGET'] != 'web'
    system "adb shell pm uninstall -k #{android_settings[:android_package]}" if ENV['PLATFORM'] == 'android'
    terminate_simulator if ENV['PLATFORM'] == 'ios'
    puts 'Terminated appium server session'
    system 'pkill node'
  else
    if ENV['TARGET'] == 'web'
      driver.quit
    else
      require 'sauce_whisk'

      ENV['SAUCE_USERNAME'] = %x[echo $SAUCE_USER].strip
      ENV['SAUCE_ACCESS_KEY'] = %x[echo $SAUCE_KEY].strip

      SauceWhisk::Jobs.change_status $driver.driver.session_id, bool_status
      driver.quit
    end
  end
end