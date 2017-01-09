/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package io.atomix;

import io.atomix.resource.ResourceService;
import org.testng.annotations.Test;

/**
 * Resource proxy test.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
@Test
public class ProxyTest {

  public void testProxy() {
    TestResourceService service = ResourceService.proxy(TestResourceService.class);
    Object value = service.doSomething();
    System.out.println(value);
  }

  public static class TestResourceService implements ResourceService {
    public Integer doSomething() {
      return 1;
    }
  }

}
